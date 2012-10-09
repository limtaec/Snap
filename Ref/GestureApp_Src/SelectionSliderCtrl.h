/*
 Copyright (c) 2001 
 Author: Konstantin Boukreev 
 E-mail: konstantin@mail.primorye.ru 

 Created: 06.11.2001 16:38:36
 Version: 1.0.0
  
 port from MFC class CSelectionSliderCtrl by Pedro Pombeiro (ppombeiro@antareslda.pt)
 original article : http://www.codeguru.com/controls/SelectionSliderCtrl.shtml

////////////////////////////////////////////////////////////////////////////
// Copyright (C) 1999 by Pedro Pombeiro
// All rights reserved
//
// Distribute freely, except: don't remove my name from the source or
// documentation (don't take credit for my work), mark your changes (don't
// get me blamed for your possible bugs), don't alter or remove this
// notice.
// No warrantee of any kind, express or implied, is included with this
// software; use at your own risk, responsibility for damages (if any) to
// anyone resulting from the use of this software rests entirely with the
// user.
//
// Send bug reports, bug fixes, enhancements, requests, flames, etc., and
// I'll try to keep a version up to date.  I can be reached as follows:
//    PPombeiro@AntaresLda.pt         (company mail account)
/////////////////////////////////////////////////////////////////////////////
*/

#ifndef _SelectionSliderCtrl_d36b0ac2_01d2_4216_a639_51ae6b8f90be
#define _SelectionSliderCtrl_d36b0ac2_01d2_4216_a639_51ae6b8f90be

#if _MSC_VER > 1000 
#pragma once
#endif // _MSC_VER > 1000

template <typename T>
class SelectionSliderCtrlT : 
	public CWindowImpl<SelectionSliderCtrlT, T>
{
	BOOL	m_bSelecting;
	BOOL	m_bAnchorIsValid;
	int		m_iAnchorSelection;

public:
	SelectionSliderCtrlT() 
	{
		m_bSelecting		= FALSE;
		m_bAnchorIsValid	= FALSE;
		m_iAnchorSelection	= -1;
	}

	DECLARE_WND_SUPERCLASS(0, T::GetWndClassName())

	BEGIN_MSG_MAP(SelectionSliderCtrlT<T>)	
		MESSAGE_HANDLER(OCM_HSCROLL, OnHScroll)
		MESSAGE_HANDLER(OCM_VSCROLL, OnVScroll)
	//	DEFAULT_REFLECTION_HANDLER()
	END_MSG_MAP()		

	LRESULT OnHScroll(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& /*bHandled*/)	
	{
		UINT nTBCode = (UINT)LOWORD(wParam);

		if (T::GetStyle() & TBS_ENABLESELRANGE)
		{
			BOOL	bEndScroll	= FALSE;
			int		iPos		= T::GetPos();
			int		iPrevPos	= iPos;

			switch (nTBCode)
			{
				case TB_PAGEUP		: iPrevPos	= iPos + T::GetPageSize(); break;
				case TB_PAGEDOWN	: iPrevPos	= iPos - T::GetPageSize(); break;
				case TB_LINEUP		: iPrevPos	= iPos + T::GetLineSize(); break;
				case TB_LINEDOWN	: iPrevPos	= iPos - T::GetLineSize(); break;
			}

			switch (nTBCode)
			{
				case TB_ENDTRACK		:
					bEndScroll	= TRUE;
					break;
				case TB_PAGEUP			:
				case TB_PAGEDOWN		:
				case TB_LINEUP			:
				case TB_LINEDOWN		:
				case TB_THUMBPOSITION	:
				case TB_THUMBTRACK		:
				{
					const BOOL	bShiftIsPressed	= (::GetKeyState(VK_SHIFT) < 0);

					if (!m_bSelecting && !bShiftIsPressed)
					{
						m_iAnchorSelection	= -1;
						m_bAnchorIsValid	= FALSE;
					}
					if (!m_bSelecting && !m_bAnchorIsValid)
					{
						int	iMin, iMax;
						T::GetSelection(iMin, iMax);
						if (iMin != -1 || iMax != -1)
							T::ClearSel(TRUE);
					}
					if (!m_bSelecting && bShiftIsPressed)
					{
						m_bSelecting	= TRUE;
						if (!m_bAnchorIsValid)
						{
							m_iAnchorSelection	= iPrevPos;
							m_bAnchorIsValid	= TRUE;
						}
					}
					if (m_bSelecting)
					{
					//	SetSelection(min(m_iAnchorSelection, iPos), max(m_iAnchorSelection, iPos));
					//	Invalidate(FALSE);					
						SendMessage(TBM_SETSEL, TRUE, 
							MAKELONG(min(m_iAnchorSelection, iPos), max(m_iAnchorSelection, iPos)));
					}
					break;
				}
				default	:
					ASSERT(FALSE);
					break;
			}

			if (bEndScroll)
			{
				if (!m_bSelecting || ::GetKeyState(VK_SHIFT) >= 0)
				{
					m_bAnchorIsValid	= FALSE;
					m_iAnchorSelection	= -1;
				}
				m_bSelecting	= FALSE;
			}
		}
		return 0;
	}
	
	LRESULT OnVScroll(UINT uMsg, WPARAM wParam, LPARAM lParam, BOOL& bHandled)
	{
		return OnHScroll(uMsg, wParam, lParam, bHandled);
	}

};

#include "AtlControls.h"
typedef SelectionSliderCtrlT<CTrackBarCtrlT<CWindow> > SelectionSliderCtrl;

#endif //_SelectionSliderCtrl_d36b0ac2_01d2_4216_a639_51ae6b8f90be

