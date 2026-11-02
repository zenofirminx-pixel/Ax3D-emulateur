#pragma once
#include <string>
#include "iso_reader.h"
#include "cpu/allegrex.h"
#include "abx_vm.h"
namespace ax3d { class Core { IsoReader iso_;Allegrex cpu_;bool loaded_=false; AbxVm abx_; bool abxLoaded_=false;std::string title_,id_;public:std::string openGame(const std::string&);std::string runFrame();void stop();}; }
