#pragma once
#include <cstdint>
#include <string>
#include <vector>
namespace ax3d { struct ElfSegment{uint32_t address;std::vector<uint8_t> bytes;};struct ElfImage{uint32_t entry;std::vector<ElfSegment> segments;};bool loadElf(const std::vector<uint8_t>&,ElfImage&,std::string&); }
