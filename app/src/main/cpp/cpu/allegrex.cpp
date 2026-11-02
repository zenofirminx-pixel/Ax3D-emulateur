#include "allegrex.h"
#include <algorithm>
#include <cstring>
namespace ax3d {
static constexpr uint32_t BASE=0x08000000,SIZE=32*1024*1024;
bool Allegrex::load(const ElfImage&e,std::string&err){ram_.assign(SIZE,0);for(auto&s:e.segments){if(s.address<BASE||uint64_t(s.address-BASE)+s.bytes.size()>SIZE){err="Segment ELF hors RAM PSP.";return false;}std::copy(s.bytes.begin(),s.bytes.end(),ram_.begin()+s.address-BASE);}memset(r_,0,sizeof(r_));pc_=e.entry;hi_=lo_=0;fault_.clear();active_=true;return true;}
bool Allegrex::rd8(uint32_t a,uint8_t&v)const{if(a<BASE||uint64_t(a-BASE)>=ram_.size())return false;v=ram_[a-BASE];return true;}
bool Allegrex::wr8(uint32_t a,uint8_t v){if(a<BASE||uint64_t(a-BASE)>=ram_.size())return false;ram_[a-BASE]=v;return true;}
uint32_t Allegrex::rd32(uint32_t a)const{uint8_t b[4];for(int i=0;i<4;i++)if(!rd8(a+i,b[i]))return 0;return uint32_t(b[0])|(uint32_t(b[1])<<8)|(uint32_t(b[2])<<16)|(uint32_t(b[3])<<24);}
void Allegrex::wr32(uint32_t a,uint32_t v){for(int i=0;i<4;i++)wr8(a+i,uint8_t(v>>(8*i)));}
uint64_t Allegrex::run(uint64_t budget){uint64_t count=0;while(active_&&count<budget){
 uint32_t ins=rd32(pc_),old=pc_;pc_+=4;uint32_t op=ins>>26,rs=(ins>>21)&31,rt=(ins>>16)&31,rd=(ins>>11)&31,sh=(ins>>6)&31,fn=ins&63,a=r_[rs],b=r_[rt],u=ins&65535;int16_t im=int16_t(u);uint32_t v=0;bool wr=false;
 switch(op){
 case 0:switch(fn){case 0:v=b<<sh;wr=true;break;case 2:v=b>>sh;wr=true;break;case 3:v=uint32_t(int32_t(b)>>sh);wr=true;break;case 8:pc_=a;break;case 9:v=pc_;wr=true;pc_=a;break;case 12:active_=false;fault_="SYSCALL : couche noyau PSP/HLE requise.";break;case 16:v=hi_;wr=true;break;case 18:v=lo_;wr=true;break;case 24:{uint64_t x=uint64_t(a)*uint64_t(b);lo_=x;hi_=x>>32;break;}case 25:{uint64_t x=uint64_t(a)*b;lo_=x;hi_=x>>32;break;}case 32:case 33:v=a+b;wr=true;break;case 34:case 35:v=a-b;wr=true;break;case 36:v=a&b;wr=true;break;case 37:v=a|b;wr=true;break;case 38:v=a^b;wr=true;break;case 39:v=~(a|b);wr=true;break;case 42:v=int32_t(a)<int32_t(b);wr=true;break;case 43:v=a<b;wr=true;break;default:active_=false;fault_="Instruction SPECIAL non prise en charge.";break;}if(wr&&rd)r_[rd]=v;break;
 case 2:pc_=(pc_&0xf0000000)|((ins&0x03ffffff)<<2);break;case 3:r_[31]=pc_;pc_=(pc_&0xf0000000)|((ins&0x03ffffff)<<2);break;
 case 4:if(a==b)pc_+=int32_t(im)*4;break;case 5:if(a!=b)pc_+=int32_t(im)*4;break;case 6:if(int32_t(a)<=0)pc_+=int32_t(im)*4;break;case 7:if(int32_t(a)>0)pc_+=int32_t(im)*4;break;
 case 8:case 9:r_[rt]=a+int32_t(im);break;case 10:r_[rt]=int32_t(a)<im;break;case 11:r_[rt]=a<uint32_t(int32_t(im));break;case 12:r_[rt]=a&u;break;case 13:r_[rt]=a|u;break;case 14:r_[rt]=a^u;break;case 15:r_[rt]=u<<16;break;
 case 32:{uint8_t x;if(rd8(a+im,x))r_[rt]=uint32_t(int32_t(int8_t(x)));else{active_=false;fault_="LB hors RAM";}break;}
 case 33:{uint8_t x,y;if(rd8(a+im,x)&&rd8(a+im+1,y))r_[rt]=uint32_t(int32_t(int16_t(x|(y<<8))));else{active_=false;fault_="LH hors RAM";}break;}
 case 35:r_[rt]=rd32(a+im);break;case 36:{uint8_t x;if(rd8(a+im,x))r_[rt]=x;else{active_=false;fault_="LBU hors RAM";}break;}
 case 37:{uint8_t x,y;if(rd8(a+im,x)&&rd8(a+im+1,y))r_[rt]=x|(y<<8);else{active_=false;fault_="LHU hors RAM";}break;}
 case 40:wr8(a+im,uint8_t(b));break;case 41:wr8(a+im,uint8_t(b));wr8(a+im+1,uint8_t(b>>8));break;case 43:wr32(a+im,b);break;
 default:active_=false;fault_="Opcode non implémenté (VFPU/FPU/COP/extension).";break;
 }r_[0]=0;count++;if(pc_==old){active_=false;fault_="PC bloqué";}
 }return count;}
}
