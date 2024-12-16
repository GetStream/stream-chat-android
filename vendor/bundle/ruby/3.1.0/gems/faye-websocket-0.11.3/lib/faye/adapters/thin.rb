# WebSocket extensions for Thin
# Based on code from the Cramp project
# http://github.com/lifo/cramp

# Copyright (c) 2009-2011 Pratik Naik
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
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

class Thin::Connection
  attr_accessor :socket_stream

  alias :thin_process      :process
  alias :thin_receive_data :receive_data

  def process
    @serving ||= nil
    if @serving != :websocket and @request.websocket?
      @serving = :websocket
    end
    if @request.socket_connection?
      @request.env['em.connection'] = self
      @response.persistent!
      @response.async = true
    end
    thin_process
  end

  def receive_data(data)
    @serving ||= nil
    return thin_receive_data(data) unless @serving == :websocket
    socket_stream.receive(data) if socket_stream
  end
end

class Thin::Request
  include Faye::WebSocket::Adapter
end

class Thin::Response
  attr_accessor :async
  alias :thin_head :head

  def head
    return '' if async and status == 101
    thin_head
  end
end
