# This code is based on the implementation in Faraday:
#
#     https://github.com/lostisland/faraday/blob/v1.0.1/lib/faraday/adapter/em_http_ssl_patch.rb
#
# Faraday is published under the MIT license as detailed here:
#
#     https://github.com/lostisland/faraday/blob/v1.0.1/LICENSE.md
#
# Copyright (c) 2009-2019 Rick Olson, Zack Hobson
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.

require 'openssl'

module Faye
  class WebSocket

    SSLError = Class.new(OpenSSL::SSL::SSLError)

    class SslVerifier
      def initialize(hostname, ssl_opts)
        @hostname   = hostname
        @ssl_opts   = ssl_opts
        @cert_store = OpenSSL::X509::Store.new

        if root = @ssl_opts[:root_cert_file]
          [root].flatten.each { |ca_path| @cert_store.add_file(ca_path) }
        else
          @cert_store.set_default_paths
        end
      end

      def ssl_verify_peer(cert_text)
        return true unless should_verify?

        certificate = parse_cert(cert_text)
        unless certificate
          raise SSLError, "Unable to parse SSL certificate for '#{ @hostname }'"
        end

        @last_cert = certificate
        @last_cert_verified = @cert_store.verify(certificate)
        store_cert(certificate) if @last_cert_verified

        true
      end

      def ssl_handshake_completed
        return unless should_verify?

        unless @last_cert_verified
          raise SSLError, "Unable to verify the server certificate for '#{ @hostname }'"
        end

        unless identity_verified?
          raise SSLError, "Host '#{ @hostname }' does not match the server certificate"
        end
      end

    private

      def should_verify?
        @ssl_opts[:verify_peer] != false
      end

      def parse_cert(cert_text)
        OpenSSL::X509::Certificate.new(cert_text)
      rescue OpenSSL::X509::CertificateError
        nil
      end

      def store_cert(certificate)
        @cert_store.add_cert(certificate)
      rescue OpenSSL::X509::StoreError => error
        raise error unless error.message =~ /cert already in hash table/
      end

      def identity_verified?
        @last_cert and OpenSSL::SSL.verify_certificate_identity(@last_cert, @hostname)
      end
    end

  end
end
