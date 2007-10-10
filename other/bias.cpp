#include <windows.h>
#include <tchar.h>
#include <string>
using namespace std;

int main(int argc,char *argv[]) {
    HWND hiddenWindow;
    AllocConsole();
    hiddenWindow = FindWindowA("ConsoleWindowClass", NULL);
    if (argc == 2 && strcmp(argv[1], "-debug") == 0) {
        ShowWindow(hiddenWindow, SW_NORMAL);
    } else {
        ShowWindow(hiddenWindow, SW_HIDE);
    }       
    STARTUPINFO si;
    PROCESS_INFORMATION pi;
    ZeroMemory( &si, sizeof(si) );
    si.cb = sizeof(si);
    ZeroMemory( &pi, sizeof(pi) );

    string classpath;
    classpath = "";
    FILE *fr; 
    char c;
    fr = fopen ("conf\\classpath.conf", "rt");
    while((c = fgetc(fr)) != -1) {
      if (c == ':') c = ';';
      classpath = classpath + c;
    }
    if (classpath.length() != 0) {
      classpath = ";" + classpath;
    }
    fclose(fr);
    
    string cmdLine = "java -cp bias.jar" + classpath + " bias.Launcher";
    printf("%s", cmdLine.c_str());
    LPTSTR szCmdline = _tcsdup(TEXT(cmdLine.c_str()));
    if (CreateProcess(NULL, szCmdline, NULL, NULL, FALSE, 0, NULL, NULL, &si, &pi)) {
        WaitForSingleObject( pi.hProcess, INFINITE );
        CloseHandle( pi.hProcess );
        CloseHandle( pi.hThread );
    }
}    
