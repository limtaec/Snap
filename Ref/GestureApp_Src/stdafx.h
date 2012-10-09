#ifndef _STDAFX_H_
#define _STDAFX_H_

#include "kbase.h"
namespace kb = kbase_2001;

#include <tchar.h>
#include <atlbase.h>
extern CComModule _Module;
#include <atlcom.h>
#include <atlwin.h>
//#include <atlhost.h>

#include <commctrl.h>

#include "ATLControls.h"
using namespace ATLControls;

#pragma warning(push, 3)
#include <vector>
#include <list>
#include <limits>
#include <algorithm>
#include <functional>
#include <numeric>
#include <iostream>
#include <fstream>
#pragma warning(pop)

#include <time.h>
#include <stdlib.h>
#include <math.h>
#include <float.h>
#include <process.h>
#include <intshcut.h>

#ifndef BEGIN_OBJECT_MAP
#define BEGIN_OBJECT_MAP(x)
#endif

#ifndef END_OBJECT_MAP
#define END_OBJECT_MAP()
#endif

#endif
