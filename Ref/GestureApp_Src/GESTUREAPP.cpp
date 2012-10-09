#include "stdafx.h"
#include "main.h"

#pragma comment (lib, "comctl32") 
#pragma comment (lib, "msimg32") 
#pragma comment (lib, "shlwapi") 

//Don't remove the Object map macros. They make the ATL wizard work
BEGIN_OBJECT_MAP(ObjectMap)
END_OBJECT_MAP()

namespace
{	
	struct _coinit
	{
		_coinit()
		{
			::CoInitialize(NULL);
		}
		~_coinit()
		{
			::CoUninitialize();
		}
	} __coinit;
}
CComModule _Module;

void RunApp();

int APIENTRY _tWinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPTSTR lpCmdLine, int nCmdShow) 
{
	_Module.Init(ObjectMap, hInstance);

	RunApp();

	_Module.Term();
	return 0;
}

void RunApp()
{
	srand(time(0));

	INITCOMMONCONTROLSEX icc = {sizeof INITCOMMONCONTROLSEX, ICC_UPDOWN_CLASS|ICC_BAR_CLASSES};
	InitCommonControlsEx(&icc);


	_Main main;
	main.Create(0, 0);
	main.ShowWindow(SW_SHOW);
	main.UpdateWindow();

	MSG msg;
	
	while (GetMessage(&msg,  0, 0, 0))
	{
		if (!main.DispatchDialogMessage(&msg)) 
		{
			DispatchMessage(&msg);
			TranslateMessage(&msg);
		}
	}
}