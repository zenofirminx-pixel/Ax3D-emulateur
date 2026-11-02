#pragma once
#include <cstdint>
#include <vector>
namespace ax3d {
struct GeCommand { uint8_t opcode; uint32_t argument; };
// Décodeur du flux de commandes GE PSP (format 8 bits opcode + 24 bits argument).
inline std::vector<GeCommand> decodeGeList(const uint32_t* words,size_t count){std::vector<GeCommand> out;out.reserve(count);for(size_t i=0;i<count;i++)out.push_back({uint8_t(words[i]>>24),words[i]&0x00ffffffu});return out;}
}
