#!/usr/bin/env python3
"""Build a minimal AX3D ABX v1 executable from raw bytecode."""
import argparse, pathlib, struct, uuid, zlib
p=argparse.ArgumentParser(description='Create AX3D Binary Executable (.abx)')
p.add_argument('bytecode',type=pathlib.Path);p.add_argument('output',type=pathlib.Path)
p.add_argument('--id',help='16-byte game UUID (default: generated)')
a=p.parse_args(); code=a.bytecode.read_bytes()
if not code or len(code)>16*1024*1024: raise SystemExit('Bytecode size must be 1..16 MiB')
gid=uuid.UUID(a.id).bytes if a.id else uuid.uuid4().bytes
header=b'AX3D'+struct.pack('<HH',1,64)+gid+struct.pack('<III',len(code),0,zlib.crc32(code)&0xffffffff)+bytes(28)
assert len(header)==64
a.output.write_bytes(header+code)
print(f'Created {a.output} ({len(code)} bytes), game id {uuid.UUID(bytes=gid)}')
