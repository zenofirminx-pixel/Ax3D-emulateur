# AX3D Binary Executable (ABX) v1

ABX is a sandboxed AX3D VM bytecode format. It is **not native ARM code** and must never be executed as machine code by Android.

## Header (64 bytes, little-endian)
- `0..3`: ASCII `AX3D` magic
- `4..5`: format version (`1`)
- `6..7`: header size (`64`)
- `8..23`: 16-byte game UUID
- `24..27`: bytecode length
- `28..31`: resource section length (v1 loader validates/skips this section)
- `32..35`: CRC-32 of bytecode only
- `36..63`: reserved, zero in v1
- `64..`: bytecode then resource payload

Maximum file size: 64 MiB; maximum code: 16 MiB; maximum resource payload: 48 MiB.

## Bytecode instructions
All opcodes are one byte. Signed immediates/targets are little-endian int32.

| Opcode | Encoding | Meaning |
|---|---|---|
| `00` | 1 byte | NOP |
| `01` | `01` + i32 | PUSH i32 |
| `10` | 1 byte | NEG |
| `11` | 1 byte | ADD (pop b,a; push a+b) |
| `12` | 1 byte | SUB |
| `13` | 1 byte | MUL |
| `14` | 1 byte | DIV (division by zero traps) |
| `20` | 1 byte | PRINT integer (diagnostic output) |
| `21` | 1 byte | POP integer address, push memory[address] |
| `22` | `22` + i32 address | POP value, store at address |
| `23` | `23` + i32 address | Load memory[address] |
| `30` | 1 byte | MOVE_X diagnostic event, pop integer |
| `31` | 1 byte | DRAW_OBJECT diagnostic event, pop object id |
| `32` | `32` + i32 target | Absolute bytecode jump |
| `40` | `40` + i32 target | Pop condition; jump if nonzero |
| `FF` | 1 byte | HALT |

The VM has a 256-cell integer sandbox memory, bounded operand stack, instruction budget and bounds checks. v1 is intentionally a small executable VM; graphics/audio opcodes currently produce diagnostic events rather than rendering a scene. CRC-32 detects accidental corruption, **not malicious tampering**. A future signed package version should use Ed25519 with an explicit trusted-key policy. A copied `AX3D` magic is not proof of publisher identity.

## Minimal test program
Hex bytecode: `01 2A 00 00 00 20 FF` — push 42, print it, halt.

Create it from Termux:
```sh
printf '\x01\x2a\x00\x00\x00\x20\xff' > hello.abxcode
python tools/create_abx.py hello.abxcode Hello-AX3D.abx
```
Import `Hello-AX3D.abx` in AX3D and press Load/Execute. The current VM prints `42` in its diagnostic output. This tests the ABX loader and VM, not a 3D game yet.
