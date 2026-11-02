#pragma once
#include <cstdint>
#include <cstdio>
#include <string>
#include <vector>
namespace ax3d {
// Lecteur CISO : décompresse les blocs zlib à la demande. Compatible avec les
// images CISO standard non segmentées (header CISO 24 octets).
class CsoReader {
 FILE* f_=nullptr; uint64_t fileSize_=0; uint32_t blockSize_=0,totalBytes_=0; uint8_t align_=0; std::vector<uint32_t> index_;
 bool block(uint32_t n,std::vector<uint8_t>& out,std::string& err);
public:
 ~CsoReader(); bool open(const std::string& path,std::string& err);
 bool read(uint64_t offset,void* dst,size_t length,std::string& err);
 uint64_t size() const{return totalBytes_;}
};
}
