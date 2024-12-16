# frozen_string_literal: true

$:.unshift(File.dirname(__FILE__)) unless
  $:.include?(File.dirname(__FILE__)) || $:.include?(File.expand_path(File.dirname(__FILE__)))

require 'cgi'
require 'forwardable'
require 'ipaddr'
require 'openssl'
require 'rbconfig'
require 'socket'
require 'timeout'
require 'uri'
require 'zlib'
require 'stringio'

require 'excon/version'

require 'excon/extensions/uri'

require 'excon/middlewares/base'
require 'excon/middlewares/expects'
require 'excon/middlewares/idempotent'
require 'excon/middlewares/instrumentor'
require 'excon/middlewares/mock'
require 'excon/middlewares/response_parser'

require 'excon/error'
require 'excon/constants'
require 'excon/utils'

require 'excon/connection'
require 'excon/headers'
require 'excon/response'
require 'excon/middlewares/decompress'
require 'excon/middlewares/escape_path'
require 'excon/middlewares/redirect_follower'
require 'excon/middlewares/capture_cookies'
require 'excon/pretty_printer'
require 'excon/socket'
require 'excon/ssl_socket'
require 'excon/instrumentors/standard_instrumentor'
require 'excon/instrumentors/logging_instrumentor'
require 'excon/unix_socket'

# Define defaults first so they will be available to other files
module Excon
  class << self
    # @return [Hash] defaults for Excon connections
    def defaults
      @defaults ||= DEFAULTS
    end

    # Change defaults for Excon connections
    # @return [Hash] defaults for Excon connections
    attr_writer :defaults

    def display_warning(warning)
      # Show warning if $VERBOSE or ENV['EXCON_DEBUG'] is set
      ($VERBOSE || ENV['EXCON_DEBUG']) && Warning.warn("[excon][WARNING] #{warning}\n#{caller.join("\n")}")

      return unless @raise_on_warnings

      raise(Error::Warning, warning)
    end

    def set_raise_on_warnings!(should_raise)
      @raise_on_warnings = should_raise
    end

    # Status of mocking
    def mock
      display_warning('Excon#mock is deprecated, use Excon.defaults[:mock] instead.')
      self.defaults[:mock]
    end

    # Change the status of mocking
    # false is the default and works as expected
    # true returns a value from stubs or raises
    def mock=(new_mock)
      display_warning('Excon#mock is deprecated, use Excon.defaults[:mock]= instead.')
      self.defaults[:mock] = new_mock
    end

    # @return [String] The filesystem path to the SSL Certificate Authority
    def ssl_ca_path
      display_warning('Excon#ssl_ca_path is deprecated, use Excon.defaults[:ssl_ca_path] instead.')
      self.defaults[:ssl_ca_path]
    end

    # Change path to the SSL Certificate Authority
    # @return [String] The filesystem path to the SSL Certificate Authority
    def ssl_ca_path=(new_ssl_ca_path)
      display_warning('Excon#ssl_ca_path= is deprecated, use Excon.defaults[:ssl_ca_path]= instead.')
      self.defaults[:ssl_ca_path] = new_ssl_ca_path
    end

    # @return [true, false] Whether or not to verify the peer's SSL certificate / chain
    def ssl_verify_peer
      display_warning('Excon#ssl_verify_peer is deprecated, use Excon.defaults[:ssl_verify_peer] instead.')
      self.defaults[:ssl_verify_peer]
    end

    # Change the status of ssl peer verification
    # @see Excon#ssl_verify_peer (attr_reader)
    def ssl_verify_peer=(new_ssl_verify_peer)
      display_warning('Excon#ssl_verify_peer= is deprecated, use Excon.defaults[:ssl_verify_peer]= instead.')
      self.defaults[:ssl_verify_peer] = new_ssl_verify_peer
    end

    # @see Connection#initialize
    # Initializes a new keep-alive session for a given remote host
    # @param [String] url The destination URL
    # @param [Hash<Symbol, >] params One or more option params to set on the Connection instance
    # @return [Connection] A new Excon::Connection instance
    def new(url, params = {})
      uri_parser = params[:uri_parser] || defaults[:uri_parser]
      uri = uri_parser.parse(url)
      params[:path] && uri_parser.parse(params[:path])
      raise(ArgumentError, "Invalid URI: #{uri}") unless uri.scheme

      params = {
        host: uri.host,
        hostname: uri.hostname,
        path: uri.path,
        port: uri.port,
        query: uri.query,
        scheme: uri.scheme
      }.merge(params)
      uri.password && params[:password] = Utils.unescape_uri(uri.password)
      uri.user && params[:user] = Utils.unescape_uri(uri.user)
      Excon::Connection.new(params)
    end

    # push an additional stub onto the list to check for mock requests
    # @param request_params [Hash<Symbol, >] request params to match against, omitted params match all
    # @param response_params [Hash<Symbol, >] response params to return or block to call with matched params
    def stub(request_params = {}, response_params = nil, &block)
      if (method = request_params.delete(:method))
        request_params[:method] = method.to_s.downcase.to_sym
      end
      if (url = request_params.delete(:url))
        uri = URI.parse(url)
        request_params = {
          host: uri.host,
          path: uri.path,
          port: uri.port,
          query: uri.query,
          scheme: uri.scheme
        }.merge!(request_params)
        if uri.user || uri.password
          request_params[:headers] ||= {}
          user = Utils.unescape_form(uri.user.to_s)
          pass = Utils.unescape_form(uri.password.to_s)
          request_params[:headers]['Authorization'] ||= 'Basic ' + ["#{user}:#{pass}"].pack('m').delete(Excon::CR_NL)
        end
      end
      if request_params.key?(:headers)
        headers = Excon::Headers.new
        request_params[:headers].each do |key, value|
          headers[key] = value
        end
        request_params[:headers] = headers
      end
      if block_given?
        raise(ArgumentError, 'stub requires either response_params OR a block') if response_params

        stub = [request_params, block]
      elsif response_params
        stub = [request_params, response_params]
      else
        raise(ArgumentError, 'stub requires either response_params OR a block')
      end
      stubs.unshift(stub)
      stub
    end

    # get a stub matching params or nil
    # @param request_params [Hash<Symbol, >] request params to match against, omitted params match all
    # @return [Hash<Symbol, >] response params to return from matched request or block to call with params
    def stub_for(request_params={})
      if (method = request_params.delete(:method))
        request_params[:method] = method.to_s.downcase.to_sym
      end
      Excon.stubs.each do |stub, response_params|
        captures = { headers: {} }
        headers_match = !stub.key?(:headers) || stub[:headers].keys.all? do |key|
          case value = stub[:headers][key]
          when Regexp
            case request_params[:headers][key]
            when String
              if (match = value.match(request_params[:headers][key]))
                captures[:headers][key] = match.captures
              end
            when Regexp # for unstub on regex params
              match = (value == request_params[:headers][key])
            end
            match
          else
            value == request_params[:headers][key]
          end
        end
        non_headers_match = (stub.keys - [:headers]).all? do |key|
          case value = stub[key]
          when Regexp
            case request_params[key]
            when String
              if (match = value.match(request_params[key]))
                captures[key] = match.captures
              end
            when Regexp # for unstub on regex params
              match = (value == request_params[key])
            end
            match
          else
            value == request_params[key]
          end
        end
        if headers_match && non_headers_match
          request_params[:captures] = captures
          return [stub, response_params]
        end
      end
      nil
    end

    # get a list of defined stubs
    def stubs
      case Excon.defaults[:stubs]
      when :global
        @stubs ||= []
      when :local
        Thread.current[:_excon_stubs] ||= []
      end
    end

    # remove first/oldest stub matching request_params or nil if none match
    # @param request_params [Hash<Symbol, >] request params to match against, omitted params match all
    # @return [Hash<Symbol, >] response params from deleted stub
    def unstub(request_params = {})
      return unless (stub = stub_for(request_params))

      Excon.stubs.delete_at(Excon.stubs.index(stub))
    end

    # Generic non-persistent HTTP methods
    HTTP_VERBS.each do |method|
      module_eval <<-DEF, __FILE__, __LINE__ + 1
        def #{method}(url, params = {}, &block)
          new(url, params).request(:method => :#{method}, &block)
        end
      DEF
    end
  end
end
