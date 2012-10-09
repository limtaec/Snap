/*
 Copyright (c) 2001 
 Author: Konstantin Boukreev 
 E-mail: konstantin@mail.primorye.ru 

 Created: 01.11.2001 17:39:39
 Version: 1.0.0

*/

/*
	bugs : sometimes, it doesn't draw anything in a top desk even if the pattern was recognized
*/

#include "stdafx.h"
#include "board.h"
#include "resource.h"
#include "GestureData.h"

const int DESK_WIDTH	= 100;
const int DESK_HEIGHT	= 100;
const int STATUS_HEIGHT	= 20;
const int STATUS_INDENT	= 2;

const double pi = 3.1415926535;

#ifdef max
#undef max
#endif

Board::Board(unsigned range) 
	: 
	NUMBER_OF_ANGLES(range),
	NUMBER_OF_ANCHOR_POINTS(range + 1),		
	m_angles (NUMBER_OF_ANGLES),
	m_cosines(NUMBER_OF_ANGLES),
	m_sinuses(NUMBER_OF_ANGLES),
	m_hpdesk(NUMBER_OF_ANGLES / 4),
	m_mode(untrained_mode)
{
	m_refresh	= false;
//	m_timestamp	= 0;	
	m_hMemDC	= 0;
	m_hBitmap	= 0;
	m_hOldBitmap = 0;
	m_hMemDC2	= 0;
	m_hBitmap2	= 0;
	m_hOldBitmap2 = 0;	
	m_hMemDC3	= 0;
	m_hBitmap3	= 0;
	m_hOldBitmap3 = 0;
	m_hMemDC4	= 0;
	m_hBitmap4	= 0;
	m_hOldBitmap4 = 0;
	
	m_hbrh1		= CreateSolidBrush(RGB(255, 255, 255));
	m_hbrh2		= CreateSolidBrush(RGB(231, 231, 231));
	m_hpen1		= CreatePen(PS_SOLID, 0, RGB(0, 0, 128));
	m_hpen2		= CreatePen(PS_SOLID, 0, RGB(192, 192, 192));
	m_hpen3		= CreatePen(PS_SOLID, 0, RGB(255, 0, 0));	
	m_hpen4		= CreatePen(PS_DOT, 0, RGB(221, 221, 221));	
	
	unsigned dc = 255 / m_hpdesk.size();
	for (unsigned i = 0; i < m_hpdesk.size(); ++i)
	{
		m_hpdesk[i] = CreatePen(PS_SOLID, 0, RGB(255 - i * dc, 0, i * dc));
	}
}

Board::~Board() 
{
	CleanupMemDC();

	if (m_hbrh1) DeleteObject(m_hbrh1);
	if (m_hbrh2) DeleteObject(m_hbrh2);
	if (m_hpen1) DeleteObject(m_hpen1);
	if (m_hpen2) DeleteObject(m_hpen2);
	if (m_hpen3) DeleteObject(m_hpen3);
	if (m_hpen4) DeleteObject(m_hpen4);

	for (unsigned i = 0; i < m_hpdesk.size(); ++i)
		if (m_hpdesk[i]) DeleteObject(m_hpdesk[i]);
}

LRESULT Board::OnPaint(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled)
{
	RECT rc;
	GetClientRect(&rc);

	PAINTSTRUCT ps;		
	HDC hDC = BeginPaint(&ps);
	Draw(hDC, rc);
	EndPaint(&ps);

	return 0;
}

LRESULT Board::OnEraseBknd(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled)
{
	return 1;
}

LRESULT Board::OnSize(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled)
{
	CleanupMemDC();
	return 0;
}

LRESULT Board::OnMouseMove(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled)
{
	if (wParam & MK_RBUTTON)
	{
		POINT pt = {LOWORD(lParam), HIWORD(lParam)};
		AddMouseRecord(pt);
	}
	return 0;
}

LRESULT Board::OnRButtonDown(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled)
{	
	// at first always tries to stop any active action (training or testing)
	CWindow(GetParent()).SendMessage(WM_COMMAND, MAKEWPARAM(ID_STOP, 0));
	StartMouseRecord();
	return 0;
}

LRESULT Board::OnRButtonUp(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled)
{
	StopMouseRecord();	
	return 0;
}

LRESULT Board::OnAsyncRefresh(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled)
{
	MSG msg; 	
	while (PeekMessage(&msg, m_hWnd, WM_ASYNC_REFRESH, WM_ASYNC_REFRESH, PM_REMOVE))
		;
	Refresh();
	return 0;
}

void Board::Draw(HDC hDC, RECT& rc)
{
	if (!m_hMemDC) 
		CreateMemDC(hDC, rc);
	
	if (m_refresh) 
	{
		DrawMemDC();
		m_refresh = false;
	}			

	BitBlt(hDC, rc.left, rc.top, rc.right - rc.left, rc.bottom - rc.top, 
		m_hMemDC, 0, 0, SRCCOPY);
	
//	m_timestamp = GetTickCount();
}

void Board::CreateMemDC(HDC hDC, RECT& rc)
{
	CleanupMemDC();		
	
	m_hMemDC	= CreateCompatibleDC(hDC);
	m_hBitmap	= CreateCompatibleBitmap(hDC, rc.right - rc.left, rc.bottom - rc.top);
	m_hOldBitmap = (HBITMAP)SelectObject(m_hMemDC, m_hBitmap);

	m_hMemDC2	= CreateCompatibleDC(hDC);
	m_hBitmap2	= CreateCompatibleBitmap(hDC, DESK_WIDTH, DESK_HEIGHT);
	m_hOldBitmap2 = (HBITMAP)SelectObject(m_hMemDC2, m_hBitmap2);

	m_hMemDC3	= CreateCompatibleDC(hDC);
	m_hBitmap3	= CreateCompatibleBitmap(hDC, rc.right - rc.left - STATUS_INDENT * 2, STATUS_HEIGHT);
	m_hOldBitmap3 = (HBITMAP)SelectObject(m_hMemDC3, m_hBitmap3);

	m_hMemDC4	= CreateCompatibleDC(hDC);
	m_hBitmap4	= CreateCompatibleBitmap(hDC, DESK_WIDTH, DESK_HEIGHT);
	m_hOldBitmap4 = (HBITMAP)SelectObject(m_hMemDC4, m_hBitmap4);

	m_refresh	= true;
	m_rcMemDC	= rc;
}

void Board::CleanupMemDC()
{
	if (m_hMemDC)
	{
		SelectObject(m_hMemDC, m_hOldBitmap);
		DeleteDC(m_hMemDC);
		DeleteObject(m_hBitmap);
		
		SelectObject(m_hMemDC2, m_hOldBitmap2);
		DeleteDC(m_hMemDC2);
		DeleteObject(m_hBitmap2);

		SelectObject(m_hMemDC3, m_hOldBitmap3);
		DeleteDC(m_hMemDC3);
		DeleteObject(m_hBitmap3);

		SelectObject(m_hMemDC4, m_hOldBitmap4);
		DeleteDC(m_hMemDC4);
		DeleteObject(m_hBitmap4);

		m_hMemDC = 0;
	}
}

void Board::DrawMemDC()
{
	unsigned cx = m_rcMemDC.right - m_rcMemDC.left;
	unsigned cy = m_rcMemDC.bottom - m_rcMemDC.top;

	int n_save_dc = SaveDC(m_hMemDC);

	FillRect(m_hMemDC, &m_rcMemDC, m_hbrh1);

	// draw grid

	SelectObject(m_hMemDC, m_hpen4);

	#define GRID_WIDTH	16
	#define GRID_HEIGHT 16

	for (int x = m_rcMemDC.left; x < m_rcMemDC.right; x += GRID_WIDTH)
	{
		MoveToEx(m_hMemDC, x, m_rcMemDC.top, 0);
		LineTo  (m_hMemDC, x, m_rcMemDC.bottom);
	}
		
	for (int y = m_rcMemDC.top; y < m_rcMemDC.bottom; y += GRID_HEIGHT)
	{
		MoveToEx(m_hMemDC, m_rcMemDC.left, y, 0);
		LineTo  (m_hMemDC, m_rcMemDC.right, y);
	}
		
	// draw main screen

	if (training_mode == m_mode || 
		trained_mode == m_mode)
	{
		RECT rc = m_rcMemDC;
		rc.top  += 3;
		rc.left += 3;
		rc.right  -= (DESK_WIDTH + 3);
		rc.bottom -= (STATUS_HEIGHT + 3);
		DrawGraph(m_hMemDC, rc);	
	}			
	else
	{
		DrawPath(m_hMemDC, m_rcMemDC);	

		if (recognized_success	== m_mode || 
			recognized_fail		== m_mode)
		{
			DrawWinners(m_hMemDC, m_rcMemDC);
		}		
	}
		
	// draw desks and status info

	int x_desk = m_rcMemDC.right - DESK_WIDTH - 3;
	int y_desk = m_rcMemDC.top   + 3;		
	RECT rcDesc = {0, 0, DESK_WIDTH, DESK_HEIGHT};

	DrawTopDesc(m_hMemDC2, rcDesc);
	BLENDFUNCTION bf = {AC_SRC_OVER, 0, 150, 0};

	AlphaBlend(m_hMemDC, x_desk, y_desk, DESK_WIDTH, DESK_HEIGHT, 
		m_hMemDC2, 0, 0, DESK_WIDTH, DESK_HEIGHT, bf);

	y_desk += (DESK_HEIGHT + 3);

	DrawBottomDesc(m_hMemDC2, rcDesc);
	bf.SourceConstantAlpha = 170; 
	AlphaBlend(m_hMemDC, x_desk, y_desk, DESK_WIDTH, DESK_HEIGHT, 
		m_hMemDC2, 0, 0, DESK_WIDTH, DESK_HEIGHT, bf);
	bf.SourceConstantAlpha = 150;

	RECT rcStatus = {0, 0, cx - STATUS_INDENT * 2, STATUS_HEIGHT};
	DrawStatusInfo(m_hMemDC3, rcStatus);
	AlphaBlend(m_hMemDC, STATUS_INDENT, cy - STATUS_HEIGHT, cx - STATUS_INDENT * 2, STATUS_HEIGHT, 
		m_hMemDC3, 0, 0, cx - STATUS_INDENT * 2, STATUS_HEIGHT, bf);

	RestoreDC(m_hMemDC, n_save_dc);
}

void Board::DrawWinners(HDC hDC, RECT& rc)
{
	int n_save_dc = SaveDC(hDC);

	SetBkMode   (hDC, TRANSPARENT);	
	SetTextColor(hDC, RGB(191, 191, 191));
	SelectObject(hDC, GetStockObject(DEFAULT_GUI_FONT));

	char buf[255] = {0};
	SIZE sz;
	int x = rc.left + 3; 
	int y = rc.top  + 3;
	for (unsigned n = 0; n < sizeof(m_winners)/sizeof(m_winners[0]); ++n)
	{	
		sprintf(buf, "%s - %f", pattern_names[m_winners[n].m_id], m_winners[n].m_probability);
		TextOut(hDC, x, y, buf, lstrlen(buf));	
		GetTextExtentPoint32(hDC, buf, lstrlen(buf), &sz);
		y += sz.cy;
	}

	RestoreDC(hDC, n_save_dc);
}

void Board::DrawPath(HDC hDC, RECT& rc)
{
	typedef path_t::iterator iterator;
	iterator i;

	// draw a mouse's path

	for (i = m_path.begin(); i != m_path.end(); ++i)
	{
		POINT& pt = (*i);
		SetPixel(hDC, pt.x, pt.y, RGB(255, 0, 0));	
	}		

	_ASSERTE((m_path2.size() == NUMBER_OF_ANCHOR_POINTS) || (m_path2.size() == 0));

	// draw a smoothed path

	for (i = m_path2.begin(); i != m_path2.end(); ++i)
	{
		POINT& pt = (*i);

		SetPixel(hDC, pt.x - 1, pt.y + 0, RGB(0, 0, 255));
		SetPixel(hDC, pt.x + 1, pt.y + 0, RGB(0, 0, 255));
		SetPixel(hDC, pt.x + 0, pt.y + 1, RGB(0, 0, 255));
		SetPixel(hDC, pt.x + 0, pt.y - 1, RGB(0, 0, 255));
		SetPixel(hDC, pt.x + 0, pt.y + 0, RGB(0, 0, 255));
	}		
}

void Board::DrawGraph(HDC hDC, RECT& rc)
{
	int n_save_dc = SaveDC(hDC);

	SelectObject(hDC, m_hpen3);	

	typedef vector_real_t::iterator iterator;
	iterator i;

	int x = rc.left;		
	double ratio = ((double)rc.bottom - rc.top) / m_max_error;
	
	for (i = m_errors.begin(); i != m_errors.end(); ++i)
	{
		MoveToEx(hDC, x, rc.bottom, 0);
		LineTo(hDC, x, rc.bottom - int((*i) * ratio));
		++x;
	}		

	RestoreDC(hDC, n_save_dc);
}

void Board::DrawTopDesc(HDC hDC, RECT& rc)
{
	int n_save_dc = SaveDC(hDC);

	SelectObject(hDC, m_hbrh2);
	SelectObject(hDC, m_hpen2);
	Rectangle(hDC, rc.left, rc.top, rc.right, rc.bottom);

	if ((recognizing_mode	== m_mode && m_path2.size()) ||
		(recognized_success	== m_mode && m_path2.size()) ||
		(recognized_fail	== m_mode && m_path2.size()) ||
		(training_mode		== m_mode && m_update_top_desk))
	{
		SelectObject(hDC, m_hpen3);
		DrawVector(hDC, rc, m_angles);
	}
	
	RestoreDC(hDC, n_save_dc);
}

void Board::DrawBottomDesc(HDC hDC, RECT& rc)
{		
	if ((training_mode == m_mode || trained_mode == m_mode)
		&& m_update_bottom_desk)
	{
		BitBlt(hDC, 0, 0, rc.right - rc.left, rc.bottom - rc.top, m_hMemDC4, 0, 0, SRCCOPY);
	}
	else
	{
		int n_save_dc = SaveDC(hDC);
		SelectObject(hDC, m_hbrh2);
		SelectObject(hDC, m_hpen2);
		Rectangle(hDC, rc.left, rc.top, rc.right, rc.bottom);

		if (recognized_success == m_mode)
		{
			//  draw a recognized pattern

			vector_real_t vec(NUMBER_OF_ANGLES);			
			for (unsigned n = 0; n < vec.size(); ++n)
				vec[n] = pattern_data[m_winners[0].m_id][n];

			SelectObject(hDC, m_hpen3);
			DrawVector(hDC, rc, vec);
		}

		RestoreDC(hDC, n_save_dc);
	}
}

void Board::DrawStatusInfo(HDC hDC, RECT& rc)
{
	int n_save_dc = SaveDC(hDC);

	SelectObject(hDC, m_hbrh2);
	SelectObject(hDC, m_hpen2);
	Rectangle(hDC, rc.left, rc.top, rc.right, rc.bottom);

	COLORREF clrTxt = RGB(0, 0, 0);

	TCHAR buf[512] = {0};

	if		(recognizing_mode == m_mode)
		sprintf(buf, "points %u", m_path.size());
	else if (training_mode == m_mode && m_errors.size())
		sprintf(buf, "error: %f", m_errors.back());	
	else if (trained_mode == m_mode && m_errors.size())
		sprintf(buf, "training cycles: %u, mean square error: %f", m_train_cycles, m_errors.back());	
	else if (recognized_success == m_mode)
	{
		clrTxt	= RGB(0, 0, 255);	
		sprintf(buf, "recognition is successful: %s (%f)", 
			pattern_names[m_winners[0].m_id], m_winners[0].m_probability);
	}
	else if (recognized_fail == m_mode)
	{
		clrTxt	= RGB(255, 0, 0);
		sprintf(buf, "unable to recognize anything, the best is %s (%f), points %u", 
			pattern_names[m_winners[0].m_id], m_winners[0].m_probability, m_path.size());
	}
	else 
		sprintf(buf, "untrained net");

	if (m_info.size())
	{
		_tcscat (buf, _T(" - "));
		_tcsncat(buf, m_info.c_str(), sizeof(buf)/sizeof(buf[0]) - _tcslen(buf));
	}
				
	SetBkMode(hDC, TRANSPARENT);	
	SetTextColor(hDC, clrTxt);
	SelectObject(hDC, GetStockObject(DEFAULT_GUI_FONT));
	SIZE sz;
	GetTextExtentPoint32(hDC, buf, lstrlen(buf), &sz);
	int x = rc.left + 3; 
	int y = rc.top + ((rc.bottom - rc.top) - sz.cy) / 2;
	ExtTextOut(hDC, x, y, ETO_CLIPPED, &rc, buf, lstrlen(buf), 0);	

	RestoreDC(hDC, n_save_dc);
}

void Board::DrawVector(HDC hDC, RECT& rc, vector_real_t& vec)
{
	typedef path_t::iterator iterator;

	path_t path;
	vector_to_path(rc, vec, path);

	iterator i = path.begin();
	MoveToEx(hDC, (*i).x, (*i).y, 0);
	++i;

	unsigned n = 0;
	for (; i != path.end(); ++i, ++n)
	{
		SelectObject(hDC, m_hpdesk[n / 4]);
		LineTo  (hDC, (*i).x, (*i).y);
		SetPixel(hDC, (*i).x, (*i).y, RGB(0, 0, 255));	
	}	
}

/////////////////////////////////////////////////

bool Board::TransfromPath()
{
	typedef path_t::iterator iterator;

	m_path2.assign(m_path.begin(), m_path.end());
		
	if (NUMBER_OF_ANCHOR_POINTS > m_path.size())
	{
		// todo : stretch path or warning message;		
		//	m_path.clear();
		m_path2.clear();
		m_mode = recognizing_mode;
		return false;
	}	
	else if (NUMBER_OF_ANCHOR_POINTS < m_path.size())
	{
		// smooth path		
		// finds smallest interval and replaces two points on median point

		while (m_path2.size() > NUMBER_OF_ANCHOR_POINTS)
		{
			double d;
			double d_min = std::numeric_limits<double>::max();
			
			iterator p_min  = m_path2.begin();
			++p_min;

			iterator p		= p_min;
			iterator i		= p_min;
			++i;

			iterator last	= m_path2.end();
			--last;

			for (; i != last; ++i)
			{
				d = sqrt(pow((*p).x - (*i).x, 2) + pow((*p).y - (*i).y, 2));
				if (d < d_min)
				{
					d_min = d;
					p_min = p;
				}
				p = i;
			}

			p = p_min;
			i = ++p_min;

			POINT pt = {((*p).x + (*i).x) / 2, ((*p).y + (*i).y) / 2};
			*i = pt;				// changes coord of a base point
			m_path2.erase(p);		// erases an odd point 
		}
	}		
	else
	{
		// nothing		
	}		
	
	_ASSERTE(m_path2.size() == NUMBER_OF_ANCHOR_POINTS);

	// computes angles, cosines and sines

	iterator i = m_path2.begin();
	iterator p = i++;
	unsigned n = 0;

	for (; i != m_path2.end(); ++i, ++n)
	{
		POINT pt2 = (*i);		
		POINT pt1 = (*p);		

		pt2.x -= pt1.x;
		pt2.y -= pt1.y;

		if (pt2.x || pt2.y)
		{
			m_cosines[n] = float(pt2.y / sqrt(pt2.x * pt2.x + pt2.y * pt2.y));
			m_sinuses[n] = (float)sqrt(1. - m_cosines[n] * m_cosines[n]);		
			if (pt2.x < 0) m_sinuses[n] = - m_sinuses[n];		
			m_angles[n] = float(acos(m_cosines[n]) * 180. / pi);
			if (pt2.x < 0) m_angles[n] = 360.f - m_angles[n];
		}
		else
		{
			m_cosines[n] = 1;
			m_sinuses[n] = 0;
			m_angles[n]  = 0;
		}
		
		p = i;

		_ASSERTE(m_cosines[n] <=  1.);
		_ASSERTE(m_cosines[n] >= -1.);
		_ASSERTE(m_sinuses[n] <=  1.);
		_ASSERTE(m_sinuses[n] >= -1.);
		_ASSERTE(m_angles [n] <= 360.);
		_ASSERTE(m_angles [n] >= 0.);		
	}	

	return true;
}

///////////////////////////////////////////////////////////

unsigned Board::StartTraining()
{
	m_mode = training_mode;
	m_update_top_desk = false;
	m_update_bottom_desk = false;
	m_path.clear();
	m_path2.clear();
	m_max_error = 0;	
	m_errors.clear();
	m_errors.reserve(m_rcMemDC.right - m_rcMemDC.left - DESK_WIDTH - 6);	
//	Refresh();
	return m_rcMemDC.right - m_rcMemDC.left - DESK_WIDTH - 6;
}

void Board::UpdateTrainingError(float current_error)
{
	m_max_error = std::_MAX(m_max_error, current_error);
	m_errors.push_back(current_error);
//	Refresh();
}

void Board::UpdateTrainingVector(vector_real_t& vec)
{
	_ASSERTE(vec.size() == m_angles.size());

	typedef vector_real_t::iterator iterator;
	for (iterator to = m_angles.begin(), from = vec.begin(); to != m_angles.end(); ++to, ++from)
	{
		*to = *from;
	}
	m_update_top_desk = true;
}

void Board::UpdateTrainingWeight(vector_real_t::iterator w_begin, vector_real_t::iterator w_end)
{
	_ASSERTE((w_end - w_begin) <= DESK_WIDTH * DESK_HEIGHT);

	typedef vector_real_t::iterator iterator;

	int n_save_dc = SaveDC(m_hMemDC4);

	RECT rc = {0, 0, DESK_WIDTH, DESK_HEIGHT};
	
	#define SPACE 2

	SelectObject(m_hMemDC4, m_hbrh2);
	SelectObject(m_hMemDC4, m_hpen2);
	Rectangle(m_hMemDC4, rc.left, rc.top, rc.right, rc.bottom);

	unsigned dim = (unsigned)sqrt(w_end - w_begin);

	unsigned left = (DESK_WIDTH - dim * SPACE) / 2;
	unsigned top  = (DESK_HEIGHT - dim * SPACE) / 2;
		
	iterator i = w_begin;
	for (unsigned x = left; x < left + dim * SPACE; x += SPACE)
		for (unsigned y = top; y < top + dim * SPACE; y += SPACE)
		{
			float f = *i;			
			COLORREF clr;
			if (fabs(f) > 1.) 
				clr = f < 0. ? RGB(255, 255, 0) : RGB(0, 0, 255) ;			
			else 
				clr = f < 0. ? RGB(f * 255., 0, 0) : RGB(0, f * 255., 0);

			SetPixel(m_hMemDC4, x, y, clr);

			SetPixel(m_hMemDC4, x + 0, y + 1, clr);
			SetPixel(m_hMemDC4, x + 1, y + 0, clr);
			SetPixel(m_hMemDC4, x + 1, y + 1, clr);

			++i;
		}

	_ASSERTE(i <= w_end);
	RestoreDC(m_hMemDC4, n_save_dc);
	m_update_bottom_desk = true;
}

void Board::StopTraining(unsigned last_cycle)
{
//	m_mode = recognizing_mode;
//	m_errors.clear();
	m_mode = trained_mode;
	m_train_cycles = last_cycle + 1; // 0..last_cycle
}

void Board::StartMouseRecord()
{	
//	CWindow(GetParent()).SendMessage(WM_COMMAND, MAKEWPARAM(ID_STOP, 0));
	m_path.clear();
	m_path2.clear();
	if (untrained_mode != m_mode)
		m_mode = recognizing_mode;
	Refresh();
}

void Board::AddMouseRecord(POINT pt)
{	
	m_path.push_back(pt);
	Refresh();
}

void Board::StopMouseRecord()
{	
	bool r = TransfromPath();	
	if (untrained_mode != m_mode && r)
	{		
		CWindow(GetParent()).SendMessage(WM_COMMAND, MAKEWPARAM(ID_RECOGNIZE, 0));
	}	
	Refresh();
}

///////////////////////////////////////

// static 
Board::vector_to_path(RECT& rc, vector_real_t& vec, path_t& path)
{
	typedef vector_real_t::iterator iterator;

	int y = 0, x = 0, cx = rc.right - rc.left, cy = rc.bottom - rc.top;				
	RECT borders = {0};
	iterator i;

	double dx = (double)cx / (vec.size() - 1);
	double dy = (double)cy / (vec.size() - 1);

	for (i = vec.begin(); i != vec.end(); ++i)
	{
		double angle = (double)*i;

		x += int(dx * sin(angle * (pi / 180.)));
		y += int(dy * cos(angle * (pi / 180.)));
		
		if (borders.left   > x) borders.left   = x;
		if (borders.right  < x) borders.right  = x;
		if (borders.top    > y) borders.top    = y;
		if (borders.bottom < y) borders.bottom = y;
	}

	x = - borders.left + (cx - (borders.right - borders.left)) / 2;
	y = - borders.top  + (cy - (borders.bottom - borders.top)) / 2; 

	POINT pt = {x, y};
	path.push_back(pt);

	unsigned n = 0;
	for (i = vec.begin(); i != vec.end(); ++i, ++n)
	{
		double angle = (double)*i;
		x += int(dx * sin(angle * (pi / 180.)));
		y += int(dy * cos(angle * (pi / 180.)));
		pt.x = x; pt.y = y;
		path.push_back(pt);
	}	
}
