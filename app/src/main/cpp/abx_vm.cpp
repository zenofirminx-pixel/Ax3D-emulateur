#include "abx_vm.h"
#include <fstream>
#include <sstream>
#include <iomanip>
#include <cstring>
#include <limits>
#include <zlib.h>
namespace ax3d {
static uint16_t u16(const uint8_t*p){return uint16_t(p[0])|(uint16_t(p[1])<<8);}
static uint32_t u32(const uint8_t*p){return uint32_t(p[0])|(uint32_t(p[1])<<8)|(uint32_t(p[2])<<16)|(uint32_t(p[3])<<24);}
static int32_t i32(const uint8_t*p){return static_cast<int32_t>(u32(p));}
bool AbxVm::load(const std::string&path,std::string&err){
 loaded_=false;halted_=false;code_.clear();gameId_.clear();
 std::ifstream f(path,std::ios::binary);if(!f){err="ABX: impossible d'ouvrir le fichier";return false;}
 f.seekg(0,std::ios::end);auto n=f.tellg();if(n<64||n>64+64*1024*1024){err="ABX: taille de fichier invalide";return false;}f.seekg(0);
 std::vector<uint8_t>b(static_cast<size_t>(n));if(!f.read(reinterpret_cast<char*>(b.data()),n)){err="ABX: lecture incomplète";return false;}
 if(std::memcmp(b.data(),"AX3D",4)!=0){err="ABX: signature AX3D absente";return false;}
 if(u16(&b[4])!=1||u16(&b[6])!=64){err="ABX: version ou taille d'en-tête non prise en charge";return false;}
 uint32_t cs=u32(&b[24]),rs=u32(&b[28]),crc=u32(&b[32]);
 if(cs==0||cs>16*1024*1024||rs>48*1024*1024||uint64_t(64)+cs+rs!=b.size()){err="ABX: tailles incohérentes";return false;}
 if(crc32(0,b.data()+64,cs)!=crc){err="ABX: CRC32 incorrect, fichier altéré";return false;}
 // Pré-validation des frontières d'instructions : aucune instruction tronquée.
 size_t pc=0;while(pc<cs){uint8_t op=b[64+pc];size_t len=0;switch(op){case 0x00:case 0x10:case 0x11:case 0x12:case 0x13:case 0x14:case 0x20:case 0x21:case 0x30:case 0x31:case 0xFF:len=1;break;case 0x01:case 0x22:case 0x23:case 0x32:case 0x40:len=5;break;default:err="ABX: opcode inconnu à l'offset "+std::to_string(pc);return false;}if(pc+len>cs){err="ABX: instruction tronquée";return false;}pc+=len;}
 code_.assign(b.begin()+64,b.begin()+64+cs);std::ostringstream id;id<<std::hex<<std::setfill('0');for(int i=0;i<16;i++)id<<std::setw(2)<<unsigned(b[8+i]);gameId_=id.str();loaded_=true;err.clear();return true;
}
std::string AbxVm::run(uint32_t budget){if(!loaded_)return "ABX: aucun programme chargé";std::vector<int32_t> st;int32_t mem[256]={};size_t pc=0;uint32_t steps=0;std::ostringstream out;auto pop=[&](int32_t&v){if(st.empty())return false;v=st.back();st.pop_back();return true;};
 while(pc<code_.size()&&steps++<budget&&!halted_){uint8_t op=code_[pc++];int32_t a,b;switch(op){case 0x00:break;case 0x01:st.push_back(i32(&code_[pc]));pc+=4;break;case 0x10:if(!pop(a))return "ABX VM: pile vide";st.push_back(-a);break;case 0x11:case 0x12:case 0x13:case 0x14:if(!pop(b)||!pop(a))return "ABX VM: pile insuffisante";if(op==0x11)st.push_back(a+b);if(op==0x12)st.push_back(a-b);if(op==0x13)st.push_back(a*b);if(op==0x14){if(!b)return "ABX VM: division par zéro";st.push_back(a/b);}break;case 0x20:if(!pop(a))return "ABX VM: pile vide";out<<"PRINT "<<a<<"\n";break;case 0x21:if(!pop(a))return "ABX VM: pile vide";if(a<0||a>=256)return "ABX VM: adresse mémoire invalide";st.push_back(mem[a]);break;case 0x22:a=i32(&code_[pc]);pc+=4;if(a<0||a>=256||!pop(b))return "ABX VM: STORE invalide";mem[a]=b;break;case 0x23:a=i32(&code_[pc]);pc+=4;if(a<0||a>=256)return "ABX VM: LOAD invalide";st.push_back(mem[a]);break;case 0x30:if(!pop(a))return "ABX VM: pile vide";out<<"MOVE_X "<<a<<"\n";break;case 0x31:if(!pop(a))return "ABX VM: pile vide";out<<"DRAW_OBJECT "<<a<<"\n";break;case 0x32:a=i32(&code_[pc]);pc+=4;if(a<0||static_cast<size_t>(a)>=code_.size())return "ABX VM: saut hors programme";pc=static_cast<size_t>(a);break;case 0x40:if(!pop(a))return "ABX VM: pile vide";if(a){int32_t target=i32(&code_[pc]);pc+=4;if(target<0||static_cast<size_t>(target)>=code_.size())return "ABX VM: branchement invalide";pc=static_cast<size_t>(target);}else pc+=4;break;case 0xFF:halted_=true;break;default:return "ABX VM: opcode invalide";}if(st.size()>4096)return "ABX VM: pile trop grande";}
 if(steps>=budget&&!halted_) { return "ABX VM: budget d'instructions dépassé\n"+out.str(); }
 return "ABX VM exécutée\nID: "+gameId_+"\nInstructions: "+std::to_string(steps)+"\n"+(halted_?"Programme terminé\n":"Fin du code\n")+out.str();
}
void AbxVm::stop(){loaded_=false;halted_=true;code_.clear();}
}
