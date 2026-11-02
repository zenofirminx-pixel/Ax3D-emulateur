#include "core.h"
#include "sfo.h"
#include "elf_loader.h"
#include <cctype>
#include <cstdio>
namespace ax3d {
std::string Core::openGame(const std::string&p){
 loaded_=false; abxLoaded_=false;
 if(p.size()>=4 && p.substr(p.size()-4)==".abx") { std::string err; if(!abx_.load(p,err)) return "REFUSÉ : "+err; abxLoaded_=true; return "ABX GAME READY\nSignature AX3D validée\nID : "+abx_.gameId()+"\nBytecode validé et chargé dans la VM isolée."; }
if(p.size()>=4&&std::string(p.end()-4,p.end())==".cso")return "CSO détecté : décompression CSO non intégrée dans cette alpha. Importez une image ISO.";
 std::string e;if(!iso_.open(p,e))return "ERREUR ISO : "+e;std::vector<uint8_t>sfo,bin;
 if(!iso_.readFile("PSP_GAME/PARAM.SFO",sfo,e))return "ERREUR ISO : "+e;title_=sfoValue(sfo,"TITLE");id_=sfoValue(sfo,"DISC_ID");
 if(!iso_.readFile("PSP_GAME/SYSDIR/EBOOT.BIN",bin,e))return "ERREUR EXE : "+e;
 ElfImage elf;if(!loadElf(bin,elf,e))return "NON DÉMARRÉ : "+e;
 if(!cpu_.load(elf,e))return "ERREUR CPU : "+e;loaded_=true;
 return "GAME READY\nTitre : "+(title_.empty()?"Inconnu":title_)+"\nID : "+(id_.empty()?"Inconnu":id_)+"\nELF MIPS chargé. Les fonctions système PSP et le GPU GE ne sont pas encore complets.";
}
std::string Core::runFrame(){if(abxLoaded_)return abx_.run();if(!loaded_)return "Aucun programme chargé.";auto n=cpu_.run(100000);char b[32];snprintf(b,sizeof(b),"%08X",cpu_.pc());return "Instructions : "+std::to_string(n)+"\nPC : 0x"+b+"\n"+(cpu_.active()?"CPU actif":"CPU arrêté")+(cpu_.fault().empty()?"":"\n"+cpu_.fault());}
void Core::stop(){cpu_.stop();abx_.stop();loaded_=false;abxLoaded_=false;}
}
