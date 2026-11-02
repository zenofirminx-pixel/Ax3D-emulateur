#pragma once
#include <cstdint>
#include <cstdio>
#include <string>
#include <vector>
namespace ax3d {
class IsoReader {
 FILE* f_=nullptr; uint64_t size_=0; uint32_t rootLba_=0,rootSize_=0;
 bool readAt(uint64_t,void*,size_t);
 bool locate(uint32_t,uint32_t,const std::string&,const std::string&,uint32_t&,uint32_t&,int);
public:
 ~IsoReader(){if(f_)fclose(f_);}
 bool open(const std::string&,std::string&);
 bool readFile(const std::string&,std::vector<uint8_t>&,std::string&);
};
}
