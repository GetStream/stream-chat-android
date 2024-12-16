### 0.11.3 / 2023-07-25

- Handle 'cert already in hash table' error message on Ruby 3.1+

### 0.11.2 / 2023-04-04

- Handle SSL certificate chains where not all the intermediate certificates are
  recognised by the client

### 0.11.1 / 2021-05-24

- Prevent the client hanging if `close()` is called when already closing

### 0.11.0 / 2020-07-31

- Implement TLS certificate verification and enable it by default on client
  connections
- Add a `:tls` option to the client with sub-fields `:root_cert_file` and
  `:verify_peer` for configuring TLS verification

### 0.10.9 / 2019-06-13

- Use the EventMachine API rather than `IO#write` to write data; this uses the
  event loop and avoids getting blocked by slow clients

### 0.10.8 / 2019-06-10

- In the case of a close timeout, don't block on waiting for writing to the
  socket to complete
- Fix a race condition that caused a timeout not to be cancelled immediately
  when the WebSocket is closed
- Change license from MIT to Apache 2.0

### 0.10.7 / 2017-02-22

- Emit an error if `EventMachine::Connection#unbind` is called with an error

### 0.10.6 / 2017-01-22

- Forcibly close the I/O stream after a timeout if the peer does not respond
  after calling `close()`

### 0.10.5 / 2016-11-12

- Set the SNI hostname when making secure requests

### 0.10.4 / 2016-05-20

- Amend warnings issued when running with -W2

### 0.10.3 / 2016-02-24

- Use `PATH_INFO` and `QUERY_STRING` rather than the non-standard `REQUEST_URI`
  from the Rack env

### 0.10.2 / 2015-11-26

- Fix the `headers` and `status` methods on `Client`, which were broken in the
  last release

### 0.10.1 / 2015-11-06

- Make sure errors can be safely emitted if creating the driver fails
- Prevent a race condition when binding `EM.attach` to the socket

### 0.10.0 / 2015-07-08

- Add the standard `code` and `reason` parameters to the `close` method

### 0.9.2 / 2014-12-21

- Only emit `error` once, and don't emit it after `close`

### 0.9.1 / 2014-12-18

- Check that all options to the WebSocket constructor are recognized

### 0.9.0 / 2014-12-13

- Allow protocol extensions to be passed into websocket-extensions

### 0.8.0 / 2014-11-08

- Support connections via HTTP proxies

### 0.7.5 / 2014-10-04

- Allow sockets to be closed when they are in any state other than `CLOSED`

### 0.7.4 / 2014-07-06

- Stop using `define_method` to implement `Event` properties, since it blows the
  method cache
- Stop setup errors masking URI errors in `Client#initialize`
- Make the Goliath adapter compatible with goliath-1.0.4.

### 0.7.3 / 2014-04-24

- Remove an unneeded method override in the `WebSocket` class

### 0.7.2 / 2013-12-29

- Fix WebSocket detection in cases where the web server does not produce an
  `env`

### 0.7.1 / 2013-12-03

- Support the `max_length` websocket-driver option
- Expose a `message` property on `error` events

### 0.7.0 / 2013-09-09

- Allow the server to send custom headers with EventSource responses

### 0.6.3 / 2013-08-04

- Stop implicitly depending on Rack 1.4

### 0.6.2 / 2013-07-05

- Catch errors thrown by EventMachine and emit `error` and `close` events

### 0.6.1 / 2013-05-12

- Release a gem without log and pid files in it

### 0.6.0 / 2013-05-12

- Add support for custom headers

### 0.5.0 / 2013-05-05

- Extract the protocol handlers into the `websocket-driver` library
- Support the `rack.hijack` API
- Add support for Rainbows 4.5 and Puma
- Officially support JRuby and Rubinius

### 0.4.7 / 2013-02-14

- Emit the `close` event if TCP is closed before CLOSE frame is acked
- Treat the `Upgrade: websocket` header case-insensitively because of IE10
- Do not suppress headers in the Thin and Rainbows adapters unless the status is
  `101`

### 0.4.6 / 2012-07-09

- Add `Connection: close` to EventSource response

### 0.4.5 / 2012-04-06

- Add WebSocket error code `1011`.
- Handle URLs with no path correctly by sending `GET /`

### 0.4.4 / 2012-03-16

- Fix installation on JRuby with a platform-specific gem

### 0.4.3 / 2012-03-12

- Make `extconf.rb` a no-op on JRuby

### 0.4.2 / 2012-03-09

- Port masking-function C extension to Java for JRuby

### 0.4.1 / 2012-02-26

- Treat anything other than an `Array` as a string when calling `send()`
- Fix error loading UTF-8 validation code on Ruby 1.9 with `-Ku` flag

### 0.4.0 / 2012-02-13

- Add `ping()` method to server-side `WebSocket` and `EventSource`
- Buffer `send()` calls until the draft-76 handshake is complete

### 0.3.0 / 2012-01-13

- Add support for `EventSource` connections
- Support the Thin, Rainbows and Goliath web servers

### 0.2.0 / 2011-12-21

- Add support for `Sec-WebSocket-Protocol` negotiation
- Support `hixie-76` close frames and 75/76 ignored segments
- Improve performance of HyBi parsing/framing functions
- Write masking function in C

### 0.1.2 / 2011-12-05

- Make `hixie-76` sockets work through HAProxy

### 0.1.1 / 2011-11-30

- Fix `add_event_listener()` interface methods

### 0.1.0 / 2011-11-27

- Initial release, based on WebSocket components from Faye
