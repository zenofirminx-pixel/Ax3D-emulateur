#include "iso_reader.h"
#include <cstring>
namespace ax3d {
static uint32_t le32(const uint8_t*p){return uint32_t(p[0])|(uint32_t(p[1])<<8)|(uint32_t(p[2])<<16)|(uint32_t(p[3])<<24);}
bool IsoReader::readAt(uint64_t o,void*d,size_t n){if(!f_||o+n>size_||fseeko(f_,(off_t)o,SEEK_SET))return false;return fread(d,1,n,f_)==n;}
bool IsoReader::open(const std::string&p,std::string&e){
 if(f_)fclose(f_);f_=fopen(p.c_str(),"rb");if(!f_){e="Impossible d'ouvrir l'image.";return false;}
 fseeko(f_,0,SEEK_END);size_=ftello(f_);uint8_t b[2048];bool ok=false;
 for(uint32_t s=16;s<80;s++){if(!readAt(uint64_t(s)*2048,b,2048))break;
  if(b[0]==1&&!memcmp(b+1,"CD001",5)){rootLba_=le32(b+158);rootSize_=le32(b+166);ok=true;break;}
  if(b[0]==255)break;
 }
 if(!ok)e="Volume ISO-9660 primaire absent.";return ok;
}
bool IsoReader::locate(uint32_t lba,uint32_t bytes,const std::string&prefix,const std::string&w,uint32_t&outLba,uint32_t&outSize,int depth){
 if(depth>10||bytes>64*1024*1024)return false;std::vector<uint8_t>d(bytes);if(!readAt(uint64_t(lba)*2048,d.data(),bytes))return false;
 for(size_t i=0;i+34<d.size();){uint8_t n=d[i];if(!n){i=((i/2048)+1)*2048;continue;}if(i+n>d.size())break;
  uint8_t flags=d[i+25],nl=d[i+32];if(nl&&33u+nl<=n){std::string name((char*)&d[i+33],nl);
   if(name.size()==1&&(uint8_t)name[0]<=1){i+=n;continue;}
   auto semi=name.find(';');if(semi!=std::string::npos)name.resize(semi);
   std::string full=prefix.empty()?name:prefix+"/"+name;uint32_t cl=le32(&d[i+2]),cs=le32(&d[i+10]);
   if(full==w){outLba=cl;outSize=cs;return true;}
   if((flags&2)&&locate(cl,cs,full,w,outLba,outSize,depth+1))return true;
  }i+=n;
 }return false;
}
bool IsoReader::readFile(const std::string&p,std::vector<uint8_t>&out,std::string&e){
 uint32_t l=0,n=0;if(!locate(rootLba_,rootSize_,"",p,l,n,0)){e="Fichier absent de l'ISO : "+p;return false;}
 if(n>512*1024*1024){e="Fichier trop volumineux.";return false;}
 out.resize(n);if(!readAt(uint64_t(l)*2048,out.data(),n)){e="Lecture ISO interrompue.";return false;}return true;
}
}
