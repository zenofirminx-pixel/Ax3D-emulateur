#pragma once
#include <cstdint>
#include <string>
#include <vector>
#include "../elf_loader.h"
namespace ax3d {
class Allegrex {
 std::vector<uint8_t> ram_;uint32_t r_[32]{},pc_=0,hi_=0,lo_=0;bool active_=false;std::string fault_;
 bool rd8(uint32_t,uint8_t&)const;bool wr8(uint32_t,uint8_t);uint32_t rd32(uint32_t)const;void wr32(uint32_t,uint32_t);
public:bool load(const ElfImage&,std::string&);uint64_t run(uint64_t);void stop(){active_=false;}bool active()const{return active_;}uint32_t pc()const{return pc_;}const std::string& fault()const{return fault_;}
};
}
