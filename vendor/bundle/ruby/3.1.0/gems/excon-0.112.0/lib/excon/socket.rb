# frozen_string_literal: true
require 'resolv'

module Excon
  class Socket
    include Utils

    extend Forwardable

    attr_accessor :data

    # read/write drawn from https://github.com/ruby-amqp/bunny/commit/75d9dd79551b31a5dd3d1254c537bad471f108cf
    CONNECT_RETRY_EXCEPTION_CLASSES = if defined?(IO::EINPROGRESSWaitWritable) # Ruby >= 2.1
      [Errno::EINPROGRESS, IO::EINPROGRESSWaitWritable]
    else # Ruby <= 2.0
      [Errno::EINPROGRESS]
    end
    READ_RETRY_EXCEPTION_CLASSES = if defined?(IO::EAGAINWaitReadable) # Ruby >= 2.1
      [Errno::EAGAIN, Errno::EWOULDBLOCK, IO::WaitReadable, IO::EAGAINWaitReadable, IO::EWOULDBLOCKWaitReadable]
    else # Ruby <= 2.0
      [Errno::EAGAIN, Errno::EWOULDBLOCK, IO::WaitReadable]
    end
    WRITE_RETRY_EXCEPTION_CLASSES = if defined?(IO::EAGAINWaitWritable) # Ruby >= 2.1
      [Errno::EAGAIN, Errno::EWOULDBLOCK, IO::WaitWritable, IO::EAGAINWaitWritable, IO::EWOULDBLOCKWaitWritable]
    else # Ruby <= 2.0
      [Errno::EAGAIN, Errno::EWOULDBLOCK, IO::WaitWritable]
    end
    # Maps a socket operation to a timeout property.
    OPERATION_TO_TIMEOUT = {
      :connect_read => :connect_timeout,
      :connect_write => :connect_timeout,
      :read => :read_timeout,
      :write => :write_timeout
    }.freeze

    def params
      Excon.display_warning('Excon::Socket#params is deprecated use Excon::Socket#data instead.')
      @data
    end

    def params=(new_params)
      Excon.display_warning('Excon::Socket#params= is deprecated use Excon::Socket#data= instead.')
      @data = new_params
    end

    attr_reader :remote_ip

    def_delegators(:@socket, :close)


    def initialize(data = {})
      @data = data
      @nonblock = data[:nonblock]
      @port ||= @data[:port] || 80
      @read_buffer = String.new
      @read_offset = 0
      @eof = false
      @backend_eof = false

      connect
    end

    def read(max_length = nil)
      if @eof
        max_length ? nil : ''
      elsif @nonblock
        read_nonblock(max_length)
      else
        read_block(max_length)
      end
    end

    def readline
      if @nonblock
        result = String.new
        block = consume_read_buffer

        loop do
          idx = block.index("\n")

          if idx.nil?
            result << block
          else
            result << block[0..idx]
            rewind_read_buffer(block, idx)
            break
          end

          block = read_nonblock(@data[:chunk_size]) || raise(EOFError)
        end

        result
      else # nonblock/legacy
        begin
          Timeout.timeout(@data[:read_timeout]) do
            @socket.readline
          end
        rescue Timeout::Error
          raise Excon::Errors::Timeout.new('read timeout reached')
        end
      end
    end

    def write(data)
      if @nonblock
        write_nonblock(data)
      else
        write_block(data)
      end
    end

    def local_address
      unpacked_sockaddr[1]
    end

    def local_port
      unpacked_sockaddr[0]
    end

    private

    def connect
      @socket = nil
      exception = nil
      hostname = @data[:hostname]
      port = @port
      family = @data[:family]

      if @data[:proxy]
        hostname = @data[:proxy][:hostname]
        port = @data[:proxy][:port]
        family = @data[:proxy][:family]
      end

      resolver = @data[:resolv_resolver] || Resolv::DefaultResolver

      # Deprecated
      if @data[:dns_timeouts]
        Excon.display_warning('dns_timeouts is deprecated, use resolv_resolver instead.')
        dns_resolver = Resolv::DNS.new
        dns_resolver.timeouts = @data[:dns_timeouts]
        resolver = Resolv.new([Resolv::Hosts.new, dns_resolver])
      end

      resolver.each_address(hostname) do |ip|
        # already succeeded on previous addrinfo
        if @socket
          break
        end

        @remote_ip = ip
        @data[:remote_ip] = ip

        # nonblocking connect
        begin
          sockaddr = ::Socket.sockaddr_in(port, ip)
          addrinfo = Addrinfo.getaddrinfo(ip, port, family, :STREAM).first
          socket = ::Socket.new(addrinfo.pfamily, addrinfo.socktype, addrinfo.protocol)

          if @data[:reuseaddr]
            socket.setsockopt(::Socket::Constants::SOL_SOCKET, ::Socket::Constants::SO_REUSEADDR, true)
            if defined?(::Socket::Constants::SO_REUSEPORT)
              socket.setsockopt(::Socket::Constants::SOL_SOCKET, ::Socket::Constants::SO_REUSEPORT, true)
            end
          end

          if @nonblock
            socket.connect_nonblock(sockaddr)
          else
            socket.connect(sockaddr)
          end
          @socket = socket
        rescue *CONNECT_RETRY_EXCEPTION_CLASSES
          select_with_timeout(socket, :connect_write)
          begin
            socket.connect_nonblock(sockaddr)
            @socket = socket
          rescue Errno::EISCONN
            @socket = socket
          rescue SystemCallError => exception
            socket.close rescue nil
          end
        rescue SystemCallError => exception
          socket.close rescue nil if socket
        end
      end

      exception ||= Resolv::ResolvError.new("no address for #{hostname}")

      # this will be our last encountered exception
      fail exception unless @socket

      if @data[:tcp_nodelay]
        @socket.setsockopt(::Socket::IPPROTO_TCP,
                           ::Socket::TCP_NODELAY,
                           true)
      end

      if @data[:keepalive]
        if [:SOL_SOCKET, :SO_KEEPALIVE, :SOL_TCP, :TCP_KEEPIDLE, :TCP_KEEPINTVL, :TCP_KEEPCNT].all?{|c| ::Socket.const_defined? c}
          @socket.setsockopt(::Socket::SOL_SOCKET, ::Socket::SO_KEEPALIVE, true)
          @socket.setsockopt(::Socket::SOL_TCP, ::Socket::TCP_KEEPIDLE, @data[:keepalive][:time])
          @socket.setsockopt(::Socket::SOL_TCP, ::Socket::TCP_KEEPINTVL, @data[:keepalive][:intvl])
          @socket.setsockopt(::Socket::SOL_TCP, ::Socket::TCP_KEEPCNT, @data[:keepalive][:probes])
        else
          Excon.display_warning('Excon::Socket keepalive was set, but is not supported by Ruby version.')
        end
      end
    end

    # Consume any bytes remaining in the read buffer before making a system call.
    def consume_read_buffer
      block = @read_buffer[@read_offset..-1]

      @read_offset = @read_buffer.length

      block
    end

    # Rewind the read buffer to just after the given index.
    # The offset is moved back to the start of the current chunk and then forward until just after the index.
    def rewind_read_buffer(chunk, idx)
      @read_offset = @read_offset - chunk.length + (idx + 1)
      @eof = false
    end

    def read_nonblock(max_length)
      begin
        if @read_offset != 0 && @read_offset >= @read_buffer.length
          # Clear the buffer so we can test for emptiness below
          @read_buffer.clear
          # Reset the offset so it matches the length of the buffer when empty.
          @read_offset = 0
        end

        if max_length
          until @backend_eof || readable_bytes >= max_length
            if @read_buffer.empty?
              # Avoid allocating a new buffer string when the read buffer is empty
              @read_buffer = @socket.read_nonblock(max_length, @read_buffer)
            else
              @read_buffer << @socket.read_nonblock(max_length - readable_bytes)
            end
          end
        else
          until @backend_eof
            if @read_buffer.empty?
              # Avoid allocating a new buffer string when the read buffer is empty
              @read_buffer = @socket.read_nonblock(@data[:chunk_size], @read_buffer)
            else
              @read_buffer << @socket.read_nonblock(@data[:chunk_size])
            end
          end
        end
      rescue OpenSSL::SSL::SSLError => error
        if error.message == 'read would block'
          if @read_buffer.empty?
            select_with_timeout(@socket, :read) && retry
          end
        else
          raise(error)
        end
      rescue *READ_RETRY_EXCEPTION_CLASSES
        if @read_buffer.empty?
          # if we didn't read anything, try again...
          select_with_timeout(@socket, :read) && retry
        end
      rescue EOFError
        @backend_eof = true
      end

      if max_length
        if @read_buffer.empty?
          # EOF met at beginning
          @eof = @backend_eof
          nil
        else
          start = @read_offset

          # Ensure that we can seek backwards when reading until a terminator string.
          # The read offset must never point past the end of the read buffer.
          @read_offset += max_length > readable_bytes ? readable_bytes : max_length
          @read_buffer[start...@read_offset]
        end
      else
        # read until EOFError, so return everything
        start = @read_offset

        @read_offset = @read_buffer.length
        @eof = @backend_eof

        @read_buffer[start..-1]
      end
    end

    def readable_bytes
      @read_buffer.length - @read_offset
    end

    def read_block(max_length)
      @socket.read(max_length)
    rescue OpenSSL::SSL::SSLError => error
      if error.message == 'read would block'
        select_with_timeout(@socket, :read) && retry
      else
        raise(error)
      end
    rescue *READ_RETRY_EXCEPTION_CLASSES
      select_with_timeout(@socket, :read) && retry
    rescue EOFError
      @eof = true
    end

    def write_nonblock(data)
      data = binary_encode(data)
      loop do
        written = nil
        begin
          # I wish that this API accepted a start position, then we wouldn't
          # have to slice data when there is a short write.
          written = @socket.write_nonblock(data)
        rescue Errno::EFAULT => error
          if OpenSSL.const_defined?(:OPENSSL_LIBRARY_VERSION) && OpenSSL::OPENSSL_LIBRARY_VERSION.split(' ')[1] == '1.0.2'
            msg = "The version of OpenSSL this ruby is built against (1.0.2) has a vulnerability
                   which causes a fault. For more, see https://github.com/excon/excon/issues/467"
            raise SecurityError.new(msg)
          else
            raise error
          end
        rescue OpenSSL::SSL::SSLError, *WRITE_RETRY_EXCEPTION_CLASSES => error
          if error.is_a?(OpenSSL::SSL::SSLError) && error.message != 'write would block'
            raise error
          else
            select_with_timeout(@socket, :write) && retry
          end
        end

        # Fast, common case.
        break if written == data.size

        # This takes advantage of the fact that most ruby implementations
        # have Copy-On-Write strings. Thusly why requesting a subrange
        # of data, we actually don't copy data because the new string
        # simply references a subrange of the original.
        data = data[written, data.size]
      end
    end

    def write_block(data)
      @socket.write(data)
    rescue OpenSSL::SSL::SSLError, *WRITE_RETRY_EXCEPTION_CLASSES => error
      if error.is_a?(OpenSSL::SSL::SSLError) && error.message != 'write would block'
        raise error
      else
        select_with_timeout(@socket, :write) && retry
      end
    end

    def select_with_timeout(socket, type)
      timeout_kind = type
      timeout = @data[OPERATION_TO_TIMEOUT[type]]

      # Check whether the request has a timeout configured.
      if @data.include?(:deadline)
        request_timeout = request_time_remaining

        # If the time remaining until the request times out is less than the timeout for the type of select,
        # use the time remaining as the timeout instead.
        if request_timeout < timeout
          timeout_kind = :request
          timeout = request_timeout
        end
      end

      select = case type
      when :connect_read
        IO.select([socket], nil, nil, timeout)
      when :connect_write
        IO.select(nil, [socket], nil, timeout)
      when :read
        IO.select([socket], nil, nil, timeout)
      when :write
        IO.select(nil, [socket], nil, timeout)
      end

      select || raise(Excon::Errors::Timeout.new("#{timeout_kind} timeout reached"))
    end

    def unpacked_sockaddr
      @unpacked_sockaddr ||= ::Socket.unpack_sockaddr_in(@socket.to_io.getsockname)
    rescue ArgumentError => e
      unless e.message == 'not an AF_INET/AF_INET6 sockaddr'
        raise
      end
    end

    # Returns the remaining time in seconds until we reach the deadline for the request timeout.
    # Raises an exception if we have exceeded the request timeout's deadline.
    def request_time_remaining
      now = Process.clock_gettime(Process::CLOCK_MONOTONIC)
      deadline = @data[:deadline]

      raise(Excon::Errors::Timeout.new('request timeout reached')) if now >= deadline

      deadline - now
    end
  end
end
