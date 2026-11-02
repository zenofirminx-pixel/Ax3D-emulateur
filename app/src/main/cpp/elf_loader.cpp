#include "elf_loader.h"
#include <cstring>
namespace ax3d {
static uint16_t u16(const uint8_t*p){return p[0]|(uint16_t(p[1])<<8);}
static uint32_t u32(const uint8_t*p){return uint32_t(p[0])|(uint32_t(p[1])<<8)|(uint32_t(p[2])<<16)|(uint32_t(p[3])<<24);}
bool loadElf(const std::vector<uint8_t>&b,ElfImage&o,std::string&e){
 if(b.size()<52||memcmp(b.data(),"\x7f""ELF",4)||b[4]!=1||b[5]!=1){e="EBOOT.BIN non ELF32 little-endian. PRX chiffré non décodé dans cette version.";return false;}
 if(u16(&b[18])!=8){e="ELF non MIPS.";return false;}uint32_t ph=u32(&b[28]);uint16_t es=u16(&b[42]),num=u16(&b[44]);
 if(es<32||uint64_t(ph)+uint64_t(es)*num>b.size()){e="Table ELF invalide.";return false;}o.entry=u32(&b[24]);o.segments.clear();
 for(uint16_t i=0;i<num;i++){auto p=&b[ph+i*es];if(u32(p)!=1)continue;uint32_t off=u32(p+4),va=u32(p+8),sz=u32(p+16);
  if(uint64_t(off)+sz>b.size()){e="Segment ELF hors fichier.";return false;}ElfSegment s{va,{b.begin()+off,b.begin()+off+sz}};o.segments.push_back(std::move(s));
 }if(o.segments.empty()){e="Aucun segment PT_LOAD.";return false;}return true;
}
}
