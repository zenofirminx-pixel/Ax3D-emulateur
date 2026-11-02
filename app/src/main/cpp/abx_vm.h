#pragma once
#include <cstdint>
#include <string>
#include <vector>
namespace ax3d {
class AbxVm {
public:
 bool load(const std::string& path, std::string& error);
 std::string run(uint32_t budget=100000);
 void stop();
 const std::string& gameId() const { return gameId_; }
private:
 std::vector<uint8_t> code_; std::string gameId_; bool loaded_=false, halted_=false;
};
}
