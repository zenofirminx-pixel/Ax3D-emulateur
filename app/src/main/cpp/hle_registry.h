#pragma once
#include <cstdint>
#include <string>
#include <unordered_map>
namespace ax3d {
// Registre HLE minimal : seules les fonctions explicitement enregistrées sont appelables.
class HleRegistry { public: using Handler=uint32_t(*)(uint32_t*,std::string&); private: std::unordered_map<uint32_t,Handler> handlers_;
public: void add(uint32_t nid,Handler h){handlers_[nid]=h;} bool invoke(uint32_t nid,uint32_t* gpr,std::string&err)const{auto i=handlers_.find(nid);if(i==handlers_.end()){err="NID HLE inconnu: "+std::to_string(nid);return false;}gpr[2]=i->second(gpr,err);return err.empty();} size_t count()const{return handlers_.size();}
}; }
