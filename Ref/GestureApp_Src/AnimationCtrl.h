/*
 Copyright (c) 2001 
 Author: Konstantin Boukreev 
 E-mail: konstantin@mail.primorye.ru 

 Created: 26.09.2001 15:50:40
 Version: 1.0.0

*/

#ifndef _AnimationCtrl_b265f359_194e_4c85_a744_d3f308039dd7
#define _AnimationCtrl_b265f359_194e_4c85_a744_d3f308039dd7

#if _MSC_VER > 1000 
#pragma once
#endif // _MSC_VER > 1000

class AnimationCtrl
	: public CWindowImpl<AnimationCtrl, CWindow>
{
	CComPtr<IPicture> m_spPicture;
	unsigned	m_now;			// current frame	
	unsigned	m_from;			// last frame
	unsigned	m_to;			// first frame
	int			m_direction;	// direction
	bool		m_mode;			// animation mode
	unsigned	m_timer;		// timer id	
	unsigned	m_width;		// frame width
	unsigned	m_height;		// frame height
//	HRGN		m_hRgn;			// clipping region
//	HDC			m_hMemDC;
//	HBITMAP		m_hBitmap;
//	HBITMAP		m_hOldBitmap;

public:
	AnimationCtrl() 
	{}
	
	DECLARE_WND_SUPERCLASS(0, _T("STATIC"))

	BEGIN_MSG_MAP(AnimationCtrl)	
		MESSAGE_HANDLER(WM_DESTROY,		OnDestroy)		
		MESSAGE_HANDLER(WM_PAINT,		OnPaint);
		MESSAGE_HANDLER(WM_ERASEBKGND,	OnEraseBkgnd)
		MESSAGE_HANDLER(WM_TIMER,		OnTimer);		
	END_MSG_MAP()

private:
	LRESULT OnDestroy(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled)
	{
		Stop();
		return 0;
	}
	LRESULT OnPaint(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled)
	{
		PAINTSTRUCT ps;
		HDC hdc = BeginPaint(&ps);
		Draw(hdc);
		EndPaint(&ps);
		return 0;
	}
	LRESULT OnEraseBkgnd(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled)
	{
		return 1;
	}
	LRESULT OnTimer(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled)
	{	
		/*
		HDC hdc = GetDC();
		SelectClipRgn(hdc, m_hRgn);
		Draw(hdc);
		SelectClipRgn(hdc, 0);
		ReleaseDC(hdc);
		*/
		NetxFrame();			
		Invalidate(FALSE);
		UpdateWindow();
		return 0;
	}

public:
	BOOL Load(const TCHAR* file)
	{
		HANDLE h = CreateFile(file, 
				GENERIC_READ, FILE_SHARE_READ, 0, 
				OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, 0);
		
		_ASSERTE(h != INVALID_HANDLE_VALUE);
		if (h == INVALID_HANDLE_VALUE)
			return FALSE;

		BOOL r = FALSE;
		LONG size = GetFileSize(h, 0);
		_ASSERTE(size != -1);		

		if (size != -1)
		{			
			VOID* p = HeapAlloc(GetProcessHeap(), 0, size);
			_ASSERTE(p);
			if (p)
			{
				DWORD read;
				r = ReadFile(h, p, size, &read, 0);
				_ASSERTE(r);
				_ASSERTE(LONG(read) == size);
				if (r && 
					LONG(read) == size)
				{
					r = Load(p, size);
				}				
				VERIFY(HeapFree(GetProcessHeap(), 0, p));
			}
		}
		
		VERIFY(CloseHandle(h));
		return r;
	}
	BOOL Load(const TCHAR* res_type, unsigned res_id)
	{
		HRSRC hRsr = FindResource(_Module.m_hInstResource, 
			MAKEINTRESOURCE(res_id), res_type);
		_ASSERTE(hRsr);
		if (!hRsr) return FALSE;

		DWORD size = SizeofResource(_Module.m_hInstResource, hRsr);
		_ASSERTE(size);
		if (!size) return FALSE;
	
		HGLOBAL hGlb = LoadResource(_Module.m_hInstResource, hRsr);
		_ASSERTE(hGlb);
		if (!hGlb) return FALSE;

		void* p = (BYTE*)LockResource(hGlb);
		_ASSERTE(p);
		BOOL r = FALSE;
		if (p)
		{
			r = Load(p, size);
		}		
		FreeResource(hGlb);
		return r;
	}
	BOOL Load(void* p, unsigned size)
	{	
		_ASSERTE(p);
		_ASSERTE(!::IsBadReadPtr(p, size));

		if (!p || 
			::IsBadReadPtr(p, size))
			return FALSE;
		
		HGLOBAL hGlobal = GlobalAlloc(GMEM_MOVEABLE, size);
		_ASSERTE(hGlobal);
		if (!hGlobal) return false;
		
		m_spPicture.Release();

		void *pg = GlobalLock(hGlobal);
		_ASSERTE(pg);
		
		if (pg)
		{	
			BOOL r, r1;
			r  = SafeMemCopy(pg, p, size);
			_ASSERTE(r);
			r1 = GlobalUnlock(hGlobal);
			_ASSERTE(!r1 && (GetLastError() == NO_ERROR));
			
			if (r)
			{					
				CComPtr<IStream> spStm;
				r = SUCCEEDED(CreateStreamOnHGlobal(hGlobal, TRUE, &spStm));
				_ASSERTE(r);
				if (r)
				{
					r = SUCCEEDED(OleLoadPicture(spStm, size, TRUE, IID_IPicture, (void**)&m_spPicture));
					_ASSERTE(r);
					_ASSERTE(m_spPicture);
				}			
			}
		}
		
		if (!m_spPicture) VERIFY(GlobalFree(hGlobal));
		return m_spPicture.p ? TRUE : FALSE;
	}
	bool Start (unsigned from, 
				unsigned to, 
				unsigned now, 
				unsigned interval, 				
				unsigned width,
				unsigned height,
				bool mode)
	{
		m_mode = mode;

		_ASSERTE(m_spPicture);
		_ASSERTE(IsWindow());

		if (!m_spPicture || 
			!IsWindow())
			return FALSE;
	
		m_from		= from;
		m_to		= to;
		m_now		= now;
		m_direction = 1;
		m_width		= width;
		m_height	= height;

		m_timer = SetTimer(0, interval);
		_ASSERTE(m_timer != 0);
		return m_timer != 0;
	}
	void Stop()
	{
		if (m_timer)
		{
			VERIFY(KillTimer(0));
			m_timer = 0;
		}			
	}
	void Pause() {}
	void Resume() {}

private:
	void Draw(HDC hdc)
	{	
		RECT rc;
		GetClientRect(&rc);

		LONG x = (rc.right - rc.left - m_width) / 2; 
		LONG y = (rc.bottom - rc.top - m_height) / 2; ;

		LONG cx(m_width), cy(m_height);		
		ConvertToHiMetrics(cx,  cy);			
		m_spPicture->Render(hdc, x, y, m_width, m_height, 
				0, (m_now + 1) * cy, cx, -cy, 0);				
	}

	void NetxFrame() 
	{
		if (m_mode)
		{			
			if (m_now == m_to) 			
				m_now = m_from;			
			else
				++m_now;
		}
		else
		{
			if (m_now == m_to) {m_direction = -1;}
			else if (m_now == m_from) {m_direction = 1;}
			m_now += m_direction;
		}								
	}

	void ConvertToHiMetrics(LONG &x, LONG &y)
	{
		SIZE szPix = {x, y}, szHi;
		AtlPixelToHiMetric(&szPix, &szHi);
		x = szHi.cx; y = szHi.cy;
	}

	BOOL SafeMemCopy(void* p1, const void* p2, unsigned size)
	{
		__try
		{
			memcpy(p1, p2, size);
		}
		__except(1)
		{
			_ASSERTE(0);
			return 0;
		}
		return 1;
	}
};

#endif //_AnimationCtrl_b265f359_194e_4c85_a744_d3f308039dd7

