#pragma once
#include <cstdint>
#include <vector>
#include <cstring>
namespace ax3d {
// Fenêtre RAM PSP 0x08000000..0x09FFFFFF, accès little-endian bornés.
class PspMemory { std::vector<uint8_t> ram_{32u*1024u*1024u,0};
public:
 bool valid(uint32_t a,size_t n=1)const{return a>=0x08000000u&&uint64_t(a-0x08000000u)+n<=ram_.size();}
 bool read(uint32_t a,void*out,size_t n)const{if(!valid(a,n))return false;memcpy(out,ram_.data()+a-0x08000000u,n);return true;}
 bool write(uint32_t a,const void*in,size_t n){if(!valid(a,n))return false;memcpy(ram_.data()+a-0x08000000u,in,n);return true;}
 uint32_t load32(uint32_t a)const{uint8_t b[4]{};read(a,b,4);return uint32_t(b[0])|(uint32_t(b[1])<<8)|(uint32_t(b[2])<<16)|(uint32_t(b[3])<<24);}
}; }
