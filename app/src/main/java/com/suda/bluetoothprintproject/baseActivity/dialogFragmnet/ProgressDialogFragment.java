//package com.suda.bluetoothprintproject.baseActivity.dialogFragmnet;
//
//import android.app.Dialog;
//import android.app.ProgressDialog;
//import android.os.Bundle;
//import android.support.v4.app.DialogFragment;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentTransaction;
//
//
///**
// * 轉圈圈啊轉圈圈
// */
//public class ProgressDialogFragment extends DialogFragment
//{
//	private static ProgressDialogFragment mFragment;
//
//	@Override
//	public Dialog onCreateDialog(final Bundle savedInstanceState)
//	{
//		final ProgressDialog dialog = new ProgressDialog(getActivity());
//		dialog.setIndeterminate(true);
//		dialog.setCancelable(false);
//
//		return dialog;
//	}
//
//	public static ProgressDialogFragment newInstance()
//	{
//		if(mFragment == null)
//		{
//			mFragment = new ProgressDialogFragment();
//			mFragment.setCancelable(false);
//		}
//		return mFragment;
//	}
//
//	@Override
//	public void show(FragmentManager manager, String tag)
//	{
//		FragmentTransaction ft = manager.beginTransaction();
//		ft.add(this, tag);
//		ft.commitAllowingStateLoss();
//	}
//}
//
