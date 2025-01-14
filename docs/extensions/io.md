# IO
This proposed extension adds support for blocking, serial, port-based IO to the MIMA.
It permits up to 2^16 = 65536 IO ports, each of which can be read from or written to.
It also specifies the behavior of special IO ports, which are used to provide basic IO functionality and introspection.
Each port is 24 bits wide, and can be read from or written to using the `IN` and `OUT` instructions.
During the execution of the MIMA, new ports may be added or removed, but their numbers must remain constant.
If a port is removed, all reads from it return 0, and all writes to it are ignored.
If a port is added, it may use a port number previously unused (including those previously used),
but it must not use a port number already in use.

## The "IN" instruction
The `IN` instruction reads from an IO port and writes the value to the Akku register.
It may take an arbitrary amount of time to complete, unless otherwise specified.
If the port is not connected to anything, the value read is undefined.

## The "OUT" instruction
The `OUT` instruction writes the value in the Akku register to an IO port.
It may take an arbitrary amount of time to complete, unless otherwise specified.
If the port is not connected to anything, the value written is ignored.

## The "OUTS" pseudo-instruction
The `OUTS` pseudo-instruction writes a series of values to an IO port.
This behaves like a loop over the provided array of values, writing each value to the port in turn.

## Special IO ports
The following IO ports are special and have a predefined behavior:
- Port 0: The IO introspection port.
  - Reading from this port without previously writing reads the highest port number that is connected.
  - Writing to this port begins an introspection request, the result of which is available for reading on this port thereafter.
    After reading the result, the port is reset to its initial state.
    Possible introspection requests are documented below.
- Port 1: The standard console port.
  - Reading from this port reads a character from the console.
    If no character is available, the value read is 0.
    Otherwise, the value is the character read represented as an ASCII value.
    Input of non-ASCII characters is undefined (but may use the upper 16 bits).
  - Writing to this port writes one character to the console.
    If the console is not ready to accept a character, the value written is ignored.
    A character is only written if it is not 0.
    Use the ASCII value of the character to write it.
    Outputting non-writeable or non-ASCII characters is undefined.
  - The console is not required to support backspace or other control characters.
  - The exact behavior of the console is undefined.
    Indeed, the console may not exist at all. In this case, all reads return 0 and all writes are ignored.

## Introspection requests
The format of an introspection request is as follows:
- The first 8 bits are the request code.
- The next 16 bits are the port number of the port to be introspected.

The following introspection requests are defined:
- Request 0: Get the device type of the port.
  Possible return values are:
  - 0: The port is not connected.
  - 1: The port uses a non-standard device type. Its behavior is specified elsewhere.
  - 2: The port is the introspection port.
  - 3: The port is a console. It behaves like the standard console port described above.
  - 4: The port is a simple, read-only device that provides a stream of program-specific data.
       Writing to this port seeks the specified relative nunmber of 24-bit words.
  - 5: This port is a speaker. This port accepts 24-bit, big-endian, signed PCM audio samples at 16000Hz.
       Writing to this port plays the sample. Reading from this port is undefined.