#include "sfo.h"
#include <cstring>
namespace ax3d {
static uint32_t u32(const uint8_t*p){return uint32_t(p[0])|(uint32_t(p[1])<<8)|(uint32_t(p[2])<<16)|(uint32_t(p[3])<<24);}
std::string sfoValue(const std::vector<uint8_t>&b,const std::string&key){
 if(b.size()<20||memcmp(b.data(),"\0PSF",4))return{};uint32_t kt=u32(&b[8]),vt=u32(&b[12]),cnt=u32(&b[16]);
 for(uint32_t i=0;i<cnt;i++){size_t x=20+i*16;if(x+16>b.size())break;uint16_t ko=b[x]|(uint16_t(b[x+1])<<8);uint32_t vo=u32(&b[x+12]);size_t k=kt+ko,v=vt+vo;
  if(k>=b.size()||v>=b.size())continue;std::string name((char*)&b[k]);if(name==key){size_t z=v;while(z<b.size()&&b[z])z++;return std::string((char*)&b[v],z-v);}
 }return{};
}
}
