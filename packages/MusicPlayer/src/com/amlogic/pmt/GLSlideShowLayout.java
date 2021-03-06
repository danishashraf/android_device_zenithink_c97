package com.amlogic.pmt;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.microedition.khronos.opengles.GL10;
import com.amlogic.Listener.MenuCallbackListener;
import com.amlogic.control.playerStatus;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

public class GLSlideShowLayout extends GLBaseLayout implements
		MenuCallbackListener {

	final static int SWITCH_TIMING = 1000;

	public final static float L = -3.2f; // left
	public final static float R = 3.2f; // right
	public final static float T = 2.0f; // top
	public final static float B = -2.0f; // bottom
	final static float N = 0.00001f; // near
	final static float S = 0.00001f; // small
	final static float F = 2.0f; // far

	final static int BCRows = 20 + 1;
	final static int BCColums = 32 + 1;

	float BoardColorBuf[][] = new float[17][BCRows * BCColums * 4];
	// 0000/0000
	// 1111/1111

	// 1111/0000/1111/0000
	// 0101/0101/0101/0101
	// 0101/1010/0101/1010
	// 1111/1111/0000/0000
	// 1100/1100/1100/1100
	// 1100/1100/0011/0011

	// .5 .5 .5 .5 / .5 .5 .5 .5 / .5 .5 .5 .5 / .5 .5 .5 .5

	// 0 0 0 0 / 1/20 1/20 1/20 1/20 / 2/20 2/20 2/20 2/20 / ....
	// 1 1 1 1 / 1-1/20 1-1/20 1-1/20 1-1/20 / 1-2/20 1-2/20 1-2/20 1-2/20 /
	// ....
	// 0 1/32 2/32 3/32 / 0 1/32 2/32 3/32 / 0 1/32 2/32 3/32 / ....
	// 1 1-1/32 1-2/32 1-3/32 / 1 1-1/32 1-2/32 1-3/32 / 1 1-1/32 1-2/32 1-3/32
	// / ....

	// 0 0 0 0 / 1/10 1/10 1/10 1/10 / 2/10 2/10 2/10 2/10 / .... / 1111 / ....
	// / 1/10 1/10 1/10 1/10 / 0 0 0 0
	// 1 1 1 1 / 1-1/10 1-1/10 1-1/10 1-1/10 / 1-2/10 1-2/10 1-2/10 1-2/10 /
	// .... / 1-1/10 1-1/10 1-1/10 1-1/10 / 1 1 1 1
	// 0 1/16 2/16 3/16....2/16 1/16 0 / 0 1/16 2/16 3/16....2/16 1/16 0 / 0
	// 1/16 2/16 3/16....2/16 1/16 0 / ....
	// 1 1-1/16 1-2/16 1-3/16....1-2/16 1-1/16 1 / 1 1-1/16 1-2/16
	// 1-3/16....1-2/16 1-1/16 1 / 1 1-1/16 1-2/16 1-3/16....1-2/16 1-1/16 1 /
	// ....

	final static float SwitchAnimParam[][][] = { {// 0: none
			{// slot0
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 1: enter left->right
			{// slot0
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					L, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 2: enter right->left
			{// slot0
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					R, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 3: enter up->down
			{// slot0
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					0, T, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 4: enter down->up
			{// slot0
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					0, B, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 5: disappear left->right
			{// slot0
					0, 0, N, R, 0, N, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 6: disappear right->left
			{// slot0
					0, 0, N, L, 0, N, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 7: disappear up->down
			{// slot0
					0, 0, N, 0, B, N, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 8: disappear down->up
			{// slot0
					0, 0, N, 0, T, N, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 9: enter disappear left->right
			{// slot0
					0, 0, 0, R, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					L, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 10: enter disappear right->left
			{// slot0
					0, 0, 0, L, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					R, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 11: enter disappear up->down
			{// slot0
					0, 0, 0, 0, B, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					0, T, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 12: enter disappear down->up
			{// slot0
					0, 0, 0, 0, T, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					0, B, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 13: enter left&up->right&down
			{// slot0
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					L, T, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 14: enter left&down->right&up
			{// slot0
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					L, B, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 15: enter right&up->left&down
			{// slot0
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					R, T, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 16: enter right&down->left&up
			{// slot0
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					R, B, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 17: disappear left&up->right&down
			{// slot0
					0, 0, N, R, B, N, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 18: disappear left&down->right&up
			{// slot0
					0, 0, N, R, T, N, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 19: disappear right&up->left&down
			{// slot0
					0, 0, N, L, B, N, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 20: disappear right&down->left&up
			{// slot0
					0, 0, N, L, T, N, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 21: enter scale center
			{// slot0
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					0, 0, 0, 0, 0, 0, // position
							S, S, S, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 22: enter scale left&up
			{// slot0
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					L, T, 0, 0, 0, 0, // position
							S, S, S, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 23: enter scale left&down
			{// slot0
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					L, B, 0, 0, 0, 0, // position
							S, S, S, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 24: enter scale right&up
			{// slot0
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					R, T, 0, 0, 0, 0, // position
							S, S, S, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 25: enter scale right&down
			{// slot0
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					R, B, 0, 0, 0, 0, // position
							S, S, S, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 26: disappear scale center
			{// slot0
					0, 0, N, 0, 0, N, // position
							1, 1, 1, S, S, S, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 27: disappear scale left&up
			{// slot0
					0, 0, N, L, T, N, // position
							1, 1, 1, S, S, S, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 28: disappear scale left&down
			{// slot0
					0, 0, N, L, B, N, // position
							1, 1, 1, S, S, S, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 29: disappear scale right&up
			{// slot0
					0, 0, N, R, T, N, // position
							1, 1, 1, S, S, S, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 30: disappear scale right&down
			{// slot0
					0, 0, N, R, B, N, // position
							1, 1, 1, S, S, S, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 31: enter scale rotate_z center
			{// slot0
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					0, 0, 0, 0, 0, 0, // position
							S, S, S, 1, 1, 1, // scale
							0, 360 * 2, 0, 0, 1, // rotation
					}, }, {// 32: enter scale rotate_z left&up
			{// slot0
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					L, T, 0, 0, 0, 0, // position
							S, S, S, 1, 1, 1, // scale
							0, 360 * 2, 0, 0, 1, // rotation
					}, }, {// 33: enter scale rotate_z left&down
			{// slot0
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					L, B, 0, 0, 0, 0, // position
							S, S, S, 1, 1, 1, // scale
							0, 360 * 2, 0, 0, 1, // rotation
					}, }, {// 34: enter scale rotate_z right&up
			{// slot0
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					R, T, 0, 0, 0, 0, // position
							S, S, S, 1, 1, 1, // scale
							0, 360 * 2, 0, 0, 1, // rotation
					}, }, {// 35: enter scale rotate_z right&down
			{// slot0
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, {// slot1
					R, B, 0, 0, 0, 0, // position
							S, S, S, 1, 1, 1, // scale
							0, 360 * 2, 0, 0, 1, // rotation
					}, }, {// 36: disappear scale rotate_z center
			{// slot0
					0, 0, N, 0, 0, N, // position
							1, 1, 1, S, S, S, // scale
							0, 360 * 2, 0, 0, 1, // rotation
					}, {// slot1
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 37: disappear scale rotate_z left&up
			{// slot0
					0, 0, N, L, T, N, // position
							1, 1, 1, S, S, S, // scale
							0, 360 * 2, 0, 0, 1, // rotation
					}, {// slot1
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 38: disappear scale rotate_z left&down
			{// slot0
					0, 0, N, L, B, N, // position
							1, 1, 1, S, S, S, // scale
							0, 360 * 2, 0, 0, 1, // rotation
					}, {// slot1
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 39: disappear scale rotate_z right&up
			{// slot0
					0, 0, N, R, T, N, // position
							1, 1, 1, S, S, S, // scale
							0, 360 * 2, 0, 0, 1, // rotation
					}, {// slot1
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 40: disappear scale rotate_z right&down
			{// slot0
					0, 0, N, R, B, N, // position
							1, 1, 1, S, S, S, // scale
							0, 360 * 2, 0, 0, 1, // rotation
					}, {// slot1
					0, 0, 0, 0, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 1, 0, // rotation
					}, }, {// 41: enter disappear rotate_y left->right
			{// slot0
					R / 2, 0, 0, R / 2, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, -120, 0, 1, 0, // rotation
					}, {// slot1
					L, 0, 0, L / 2, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							120, 0, 0, 1, 0, // rotation
					}, {// set0
					L / 2, 0, 0, L / 2, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 0, 0, // rotation
					}, {// set1
					R / 2, 0, 0, R / 2, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 0, 0, // rotation
					}, }, {// 42: enter disappear rotate_y right->left
			{// slot0
					L / 2, 0, 0, L / 2, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 120, 0, 1, 0, // rotation
					}, {// slot1
					R, 0, 0, R / 2, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							-120, 0, 0, 1, 0, // rotation
					}, {// set0
					R / 2, 0, 0, R / 2, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 0, 0, // rotation
					}, {// set1
					L / 2, 0, 0, L / 2, 0, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 0, 0, // rotation
					}, }, {// 43: enter disappear rotate_x up->down
			{// slot0
					0, B / 2, 0, 0, B / 2, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, -120, 1, 0, 0, // rotation
					}, {// slot1
					0, T, 0, 0, T / 2, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							120, 0, 1, 0, 0, // rotation
					}, {// set0
					0, T / 2, 0, 0, T / 2, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 0, 0, // rotation
					}, {// set1
					0, B / 2, 0, 0, B / 2, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 0, 0, // rotation
					}, }, {// 44: enter disappear rotate_x down->up
			{// slot0
					0, T / 2, 0, 0, T / 2, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 120, 1, 0, 0, // rotation
					}, {// slot1
					0, B, 0, 0, B / 2, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							-120, 0, 1, 0, 0, // rotation
					}, {// set0
					0, B / 2, 0, 0, B / 2, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 0, 0, // rotation
					}, {// set1
					0, T / 2, 0, 0, T / 2, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 0, 0, // rotation
					}, }, {// 45: enter disappear rotate_xy left&up->right&down
			{// slot0
					R / 2, B / 2, 0, R / 2, B / 2, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, -120, 2, 3.2f, 0, // rotation
					}, {// slot1
					L / 2, T / 2, 0, L / 2, T / 2, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							120, 0, 2, 3.2f, 0, // rotation
					}, {// set0
					L / 2, T / 2, 0, L / 2, T / 2, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 0, 0, // rotation
					}, {// set1
					R / 2, B / 2, 0, R / 2, B / 2, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 0, 0, // rotation
					}, }, {// 46: enter disappear rotate_xy left&down->right&up
			{// slot0
					R / 2, T / 2, 0, R / 2, T / 2, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, -120, -2, 3.2f, 0, // rotation
					}, {// slot1
					L / 2, B / 2, 0, L / 2, B / 2, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							120, 0, -2, 3.2f, 0, // rotation
					}, {// set0
					L / 2, B / 2, 0, L / 2, B / 2, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 0, 0, // rotation
					}, {// set1
					R / 2, T / 2, 0, R / 2, T / 2, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 0, 0, // rotation
					}, }, {// 47: enter disappear rotate_xy right&up->left&down
			{// slot0
					L / 2, B / 2, 0, L / 2, B / 2, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 120, -2, 3.2f, 0, // rotation
					}, {// slot1
					R / 2, T / 2, 0, R / 2, T / 2, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							-120, 0, -2, 3.2f, 0, // rotation
					}, {// set0
					R / 2, T / 2, 0, R / 2, T / 2, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 0, 0, // rotation
					}, {// set1
					L / 2, B / 2, 0, L / 2, B / 2, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 0, 0, // rotation
					}, }, {// 48: enter disappear rotate_xy right&down->left&up
			{// slot0
					L / 2, T / 2, 0, L / 2, T / 2, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 120, 2, 3.2f, 0, // rotation
					}, {// slot1
					R / 2, B / 2, 0, R / 2, B / 2, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							-120, 0, 2, 3.2f, 0, // rotation
					}, {// set0
					R / 2, B / 2, 0, R / 2, B / 2, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 0, 0, // rotation
					}, {// set1
					L / 2, T / 2, 0, L / 2, T / 2, 0, // position
							1, 1, 1, 1, 1, 1, // scale
							0, 0, 0, 0, 0, // rotation
					}, }, };

	private int currentSwitchMode = -1;
	private int currentDisappearColorMode = -1;
	private int currentAppearColorMode = -1;
	private float currentX = 0;
	private float currentY = 0;
	private float currentScale = 1;
	private float currentRotation = 0;

	GLSlideShowLayout(Context context, String name, String local) {
		super(context, name, "/sdcard/");
		initBoardColorBuffer();

		String data = myConProvider.getMyParam("PicRepeatMode");
		if (!data.equals("")) {
			if (data.equals("ORDER"))
				this.dataProvider.setSwitchMode(DataProvider.playmode_normal);
			else if (data.equals("FOLDER"))
				this.dataProvider.setSwitchMode(DataProvider.playmode_folder);
			else if (data.equals("RANDOM"))
				this.dataProvider.setSwitchMode(DataProvider.playmode_rand);
		}

		data = myConProvider.getMyParam("PicSwitchMode");
		if (!data.equals("")) {
			if (data.equals("Normal")) {
				setSwitchMode(0);
				setDisappearColorMode(0);
				setAppearColorMode(1);
			} else if (data.equals("Random")) {
				setSwitchMode(-1);
				setDisappearColorMode(-1);
				setAppearColorMode(-1);

			}

		}

		data = myConProvider.getMyParam("PicPlayMode");
		if (!data.equals("")) {
			if (data.equals("3S")) {
				delayAutoPlay = 3000;
			} else if (data.equals("5S")) {
				delayAutoPlay = 5000;
			} else if (data.equals("10S")) {
				delayAutoPlay = 10000;
			} else if (data.equals("HAND")) {

			}
		}

	}

	void initBoardColorBuffer() {
		int idx = 0;
		// 0 //0000/0000
		for (int i = 0; i < BCRows; i++)
			for (int j = 0; j < BCColums; j++) {
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 0] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 1] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 2] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 3] = 0;
			}
		idx++;
		// 1 //1111/1111
		for (int i = 0; i < BCRows; i++)
			for (int j = 0; j < BCColums; j++) {
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 0] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 1] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 2] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 3] = 1;
			}
		idx++;
		// 2 //1111/0000/1111/0000
		for (int i = 0; i < BCRows; i++)
			for (int j = 0; j < BCColums; j++) {
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 0] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 1] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 2] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 3] = (i % 2 == 0 ? 1
						: 0);
			}
		idx++;
		// 3 //0101/0101/0101/0101
		for (int i = 0; i < BCRows; i++)
			for (int j = 0; j < BCColums; j++) {
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 0] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 1] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 2] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 3] = (j % 2 != 0 ? 1
						: 0);
			}
		idx++;
		// 4 //0101/1010/0101/1010
		for (int i = 0; i < BCRows; i++)
			for (int j = 0; j < BCColums; j++) {
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 0] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 1] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 2] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 3] = ((i + j) % 2 != 0 ? 1
						: 0);
			}
		idx++;
		// 5 //1111/1111/0000/0000
		for (int i = 0; i < BCRows; i++)
			for (int j = 0; j < BCColums; j++) {
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 0] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 1] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 2] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 3] = (i / 2 % 2 == 0 ? 1
						: 0);
			}

		// 6 //1100/1100/1100/1100
		for (int i = 0; i < BCRows; i++)
			for (int j = 0; j < BCColums; j++) {
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 0] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 1] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 2] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 3] = (j / 2 % 2 == 0 ? 1
						: 0);
			}

		// 7 //1100/1100/0011/0011
		for (int i = 0; i < BCRows; i++)
			for (int j = 0; j < BCColums; j++) {
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 0] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 1] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 2] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 3] = ((i / 2 + j / 2) % 2 == 0 ? 1
						: 0);
			}
		idx++;
		// 8 //.5 .5 .5 .5 / .5 .5 .5 .5 / .5 .5 .5 .5 / .5 .5 .5 .5
		for (int i = 0; i < BCRows; i++)
			for (int j = 0; j < BCColums; j++) {
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 0] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 1] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 2] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 3] = 0.5f;
			}
		idx++;
		// 9 //0 0 0 0 / 1/20 1/20 1/20 1/20 / 2/20 2/20 2/20 2/20 / ....
		for (int i = 0; i < BCRows; i++)
			for (int j = 0; j < BCColums; j++) {
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 0] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 1] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 2] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 3] = i * 1.0f
						/ (BCRows - 1);
			}
		idx++;
		// 10 //1 1 1 1 / 1-1/20 1-1/20 1-1/20 1-1/20 / 1-2/20 1-2/20 1-2/20
		// 1-2/20 / ....
		for (int i = 0; i < BCRows; i++)
			for (int j = 0; j < BCColums; j++) {
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 0] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 1] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 2] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 3] = 1 - i * 1.0f
						/ (BCRows - 1);
			}
		idx++;
		// 11 //0 1/32 2/32 3/32 / 0 1/32 2/32 3/32 / 0 1/32 2/32 3/32 / ....
		for (int i = 0; i < BCRows; i++)
			for (int j = 0; j < BCColums; j++) {
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 0] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 1] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 2] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 3] = j * 1.0f
						/ (BCColums - 1);
			}
		idx++;
		// 12 //1 1-1/32 1-2/32 1-3/32 / 1 1-1/32 1-2/32 1-3/32 / 1 1-1/32
		// 1-2/32 1-3/32 / ....
		for (int i = 0; i < BCRows; i++)
			for (int j = 0; j < BCColums; j++) {
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 0] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 1] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 2] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 3] = 1 - j * 1.0f
						/ (BCColums - 1);
			}
		idx++;
		// 13 //0 0 0 0 / 1/10 1/10 1/10 1/10 / 2/10 2/10 2/10 2/10 / .... /
		// 1111 / .... / 1/10 1/10 1/10 1/10 / 0 0 0 0
		for (int i = 0; i < BCRows; i++)
			for (int j = 0; j < BCColums; j++) {
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 0] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 1] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 2] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 3] = 1
						- Math.abs(i - (BCRows - 1) / 2) * 1.0f
						/ ((BCRows - 1) / 2);
			}
		idx++;
		// 14 //1 1 1 1 / 1-1/10 1-1/10 1-1/10 1-1/10 / 1-2/10 1-2/10 1-2/10
		// 1-2/10 / .... / 1-1/10 1-1/10 1-1/10 1-1/10 / 1 1 1 1
		for (int i = 0; i < BCRows; i++)
			for (int j = 0; j < BCColums; j++) {
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 0] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 1] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 2] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 3] = Math.abs(i
						- (BCRows - 1) / 2)
						* 1.0f / ((BCRows - 1) / 2);
			}
		idx++;
		// 15 //0 1/16 2/16 3/16....2/16 1/16 0 / 0 1/16 2/16 3/16....2/16 1/16
		// 0 / 0 1/16 2/16 3/16....2/16 1/16 0 / ....
		for (int i = 0; i < BCRows; i++)
			for (int j = 0; j < BCColums; j++) {
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 0] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 1] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 2] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 3] = 1
						- Math.abs(j - (BCColums - 1) / 2) * 1.0f
						/ ((BCColums - 1) / 2);
			}
		idx++;
		// 16 //1 1-1/16 1-2/16 1-3/16....1-2/16 1-1/16 1 / 1 1-1/16 1-2/16
		// 1-3/16....1-2/16 1-1/16 1 / 1 1-1/16 1-2/16 1-3/16....1-2/16 1-1/16 1
		// / ....
		for (int i = 0; i < BCRows; i++)
			for (int j = 0; j < BCColums; j++) {
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 0] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 1] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 2] = 1;
				BoardColorBuf[idx][i * BCColums * 4 + j * 4 + 3] = Math.abs(j
						- (BCColums - 1) / 2)
						* 1.0f / ((BCColums - 1) / 2);
			}

	}

	Vector<PMTAnimation> animlist = new Vector<PMTAnimation>();

	private void stopAnims() {
		for (PMTAnimation a : animlist) {
			a.stop();
		}
		animlist.clear();
	}

	private void switchSlot() {
		synchronized (slots) {

			if (getSize() < 2)
				return;

			int sm = currentSwitchMode;
			if (sm < 0 || sm >= SwitchAnimParam.length) {
				sm = (int) (Math.random() * (SwitchAnimParam.length - 1)) + 1;
			}

			stopAnims();

			DisplaySlot slot0 = getItem(0);
			DisplaySlot slot1 = getItem(1);
			DisplaySet set0 = slot0.getDisplaySet();
			DisplaySet set1 = slot1.getDisplaySet();

			int aidx0 = currentDisappearColorMode;
			if (aidx0 < 0 || aidx0 >= BoardColorBuf.length) {
				aidx0 = (int) (Math.random() * (BoardColorBuf.length - 1)) + 1;
			}
			PMTBoardColorAnimation ama0 = new PMTBoardColorAnimation(slot0,
					new LinearInterpolator(), (long) SWITCH_TIMING / 2, 0);
			ama0.setBoardColor(BoardColorBuf[1], BoardColorBuf[aidx0]);
			PMTBoardColorAnimation ama01 = new PMTBoardColorAnimation(slot0,
					new LinearInterpolator(), (long) SWITCH_TIMING / 2, 0);
			ama01.setBoardColor(BoardColorBuf[aidx0], BoardColorBuf[0]);
			ama0.setNextAnim(ama01);
			animlist.add(ama0);

			int aidx1 = currentAppearColorMode;
			if (aidx1 < 0 || aidx1 >= BoardColorBuf.length) {
				aidx1 = (int) (Math.random() * (BoardColorBuf.length - 1)) + 1;
			}
			PMTBoardColorAnimation ama1 = new PMTBoardColorAnimation(slot1,
					new LinearInterpolator(), (long) SWITCH_TIMING / 2, 0);
			ama1.setBoardColor(BoardColorBuf[0], BoardColorBuf[aidx1]);
			PMTBoardColorAnimation ama11 = new PMTBoardColorAnimation(slot1,
					new LinearInterpolator(), (long) SWITCH_TIMING / 2, 0);
			ama11.setBoardColor(BoardColorBuf[aidx1], BoardColorBuf[1]);
			ama1.setNextAnim(ama11);
			animlist.add(ama1);

			PMTTransformAnimation am0 = new PMTTransformAnimation(slot0,
					new DecelerateInterpolator(), SWITCH_TIMING, 0);
			am0.setTranslation(SwitchAnimParam[sm][0][0],
					SwitchAnimParam[sm][0][1], SwitchAnimParam[sm][0][2],
					SwitchAnimParam[sm][0][3], SwitchAnimParam[sm][0][4],
					SwitchAnimParam[sm][0][5]);
			am0.setScale(SwitchAnimParam[sm][0][6] * GX,
					SwitchAnimParam[sm][0][7] * GY, SwitchAnimParam[sm][0][8],
					SwitchAnimParam[sm][0][9] * GX, SwitchAnimParam[sm][0][10]
							* GY, SwitchAnimParam[sm][0][11]);
			am0.setRotation(SwitchAnimParam[sm][0][12],
					SwitchAnimParam[sm][0][13], SwitchAnimParam[sm][0][14],
					SwitchAnimParam[sm][0][15], SwitchAnimParam[sm][0][16]);
			// scale very little to avoid flash before next time animation.
			PMTTransformAnimation am00 = new PMTTransformAnimation(slot0,
					new DecelerateInterpolator(), 0, 0);
			am00.setTranslation(0, 0, 0, 0, 0, 0);
			am00.setScale(SwitchAnimParam[sm][0][6], SwitchAnimParam[sm][0][7],
					SwitchAnimParam[sm][0][8], S, S, S);
			am00.setRotation(0, 0, 0, 1, 0);
			am0.setNextAnim(am00);

			PMTTransformAnimation am1 = new PMTTransformAnimation(slot1,
					new DecelerateInterpolator(), SWITCH_TIMING, 0);
			am1.setTranslation(SwitchAnimParam[sm][1][0],
					SwitchAnimParam[sm][1][1], SwitchAnimParam[sm][1][2],
					SwitchAnimParam[sm][1][3], SwitchAnimParam[sm][1][4],
					SwitchAnimParam[sm][1][5]);
			am1.setScale(SwitchAnimParam[sm][1][6] * GX,
					SwitchAnimParam[sm][1][7] * GY, SwitchAnimParam[sm][1][8],
					SwitchAnimParam[sm][1][9] * GX, SwitchAnimParam[sm][1][10]
							* GY, SwitchAnimParam[sm][1][11]);
			am1.setRotation(SwitchAnimParam[sm][1][12],
					SwitchAnimParam[sm][1][13], SwitchAnimParam[sm][1][14],
					SwitchAnimParam[sm][1][15], SwitchAnimParam[sm][1][16]);
			// scale very little to avoid flash before next time animation.
			PMTTransformAnimation am11 = new PMTTransformAnimation(slot1,
					new DecelerateInterpolator(), 0, 0);
			am11.setTranslation(0, 0, 0, 0, 0, 0);
			am11.setScale(GX, GY, 1, GX, GY, 1);
			am11.setRotation(0, 0, 0, 1, 0);
			am1.setNextAnim(am11);

			animlist.add(am0);
			animlist.add(am1);

			if (SwitchAnimParam[sm].length > 2) {
				PMTTransformAnimation ams0 = new PMTTransformAnimation(set0,
						new DecelerateInterpolator(), SWITCH_TIMING, 0);
				ams0.setTranslation(SwitchAnimParam[sm][2][0],
						SwitchAnimParam[sm][2][1], SwitchAnimParam[sm][2][2],
						SwitchAnimParam[sm][2][3], SwitchAnimParam[sm][2][4],
						SwitchAnimParam[sm][2][5]);
				ams0.setScale(SwitchAnimParam[sm][2][6],
						SwitchAnimParam[sm][2][7], SwitchAnimParam[sm][2][8],
						SwitchAnimParam[sm][2][9], SwitchAnimParam[sm][2][10],
						SwitchAnimParam[sm][2][11]);
				ams0.setRotation(SwitchAnimParam[sm][2][12],
						SwitchAnimParam[sm][2][13], SwitchAnimParam[sm][2][14],
						SwitchAnimParam[sm][2][15], SwitchAnimParam[sm][2][16]);
				PMTTransformAnimation ams00 = new PMTTransformAnimation(set0,
						new DecelerateInterpolator(), 0, 0);
				ams00.setTranslation(0, 0, 0, 0, 0, 0);
				ams00.setScale(1, 1, 1, 1, 1, 1);
				ams00.setRotation(0, 0, 0, 1, 0);
				ams0.setNextAnim(ams00);

				PMTTransformAnimation ams1 = new PMTTransformAnimation(set1,
						new DecelerateInterpolator(), SWITCH_TIMING, 0);
				ams1.setTranslation(SwitchAnimParam[sm][3][0],
						SwitchAnimParam[sm][3][1], SwitchAnimParam[sm][3][2],
						SwitchAnimParam[sm][3][3], SwitchAnimParam[sm][3][4],
						SwitchAnimParam[sm][3][5]);
				ams1.setScale(SwitchAnimParam[sm][3][6],
						SwitchAnimParam[sm][3][7], SwitchAnimParam[sm][3][8],
						SwitchAnimParam[sm][3][9], SwitchAnimParam[sm][3][10],
						SwitchAnimParam[sm][3][11]);
				ams1.setRotation(SwitchAnimParam[sm][3][12],
						SwitchAnimParam[sm][3][13], SwitchAnimParam[sm][3][14],
						SwitchAnimParam[sm][3][15], SwitchAnimParam[sm][3][16]);
				PMTTransformAnimation ams11 = new PMTTransformAnimation(set1,
						new DecelerateInterpolator(), 0, 0);
				ams11.setTranslation(0, 0, 0, 0, 0, 0);
				ams11.setScale(1, 1, 1, 1, 1, 1);
				ams11.setRotation(0, 0, 0, 1, 0);
				ams1.setNextAnim(ams11);

				animlist.add(ams0);
				animlist.add(ams1);
			}

			PMTAnimator.getInstance().startAnimations(animlist);

		}

		currentX = currentY = 0;
		currentScale = 1;
		currentRotation = 0;
	}

	// Max 48, -1 means random.
	public void setSwitchMode(int mode) {
		currentSwitchMode = mode;
	}

	public int getSwitchMode() {
		return currentSwitchMode;
	}

	// Max 16, -1 means random.
	public void setDisappearColorMode(int mode) {
		currentDisappearColorMode = mode;
	}

	public int getDisappearColorMode() {
		return currentDisappearColorMode;
	}

	public void setAppearColorMode(int mode) {
		currentAppearColorMode = mode;
	}

	public int getAppearColorMode() {
		return currentAppearColorMode;
	}

	//
	public void zoom(float fScale) {
		synchronized (slots) {
			DisplaySlot slot1 = getItem(1);
			stopAnims();

			PMTTransformAnimation am1 = new PMTTransformAnimation(slot1,
					new DecelerateInterpolator(), SWITCH_TIMING, 0);
			am1.setScale(currentScale, currentScale, currentScale, fScale,
					fScale, fScale);

			animlist.add(am1);
			PMTAnimator.getInstance().startAnimations(animlist);
		}

		currentScale = fScale;
	}

	public float getZoom() {
		return currentScale;
	}

	public void rotate(float rotation) {
		synchronized (slots) {
			DisplaySlot slot1 = getItem(1);
			stopAnims();

			PMTTransformAnimation am1 = new PMTTransformAnimation(slot1,
					new DecelerateInterpolator(), SWITCH_TIMING, 0);
			// if((rotation - currentRotation) > 180)
			// am1.setRotation(currentRotation, 360 - rotation,0,0,1);
			// else
			// if(( currentRotation - rotation) > 180)
			// am1.setRotation(360 - currentRotation, rotation,0,0,1);
			// else
			// am1.setRotation(currentRotation, rotation,0,0,1);

			float fromAngle = rotation - currentRotation;
			if (fromAngle < 0)
				fromAngle += 360;
			am1.setRotation(currentRotation, rotation, 0, 0, 1);
			animlist.add(am1);
			PMTAnimator.getInstance().startAnimations(animlist);
		}

		currentRotation = rotation;
	}

	public float getRotation() {
		return currentRotation;
	}

	public void setXYPosition(float x, float y) {
		synchronized (slots) {
			DisplaySlot slot1 = getItem(1);
			stopAnims();

			PMTTransformAnimation am1 = new PMTTransformAnimation(slot1,
					new DecelerateInterpolator(), SWITCH_TIMING, 0);
			am1.setTranslation(currentX, currentY, 0, x, y, 0);

			animlist.add(am1);
			PMTAnimator.getInstance().startAnimations(animlist);
		}

		currentX = x;
		currentY = y;
	}

	public float getXPosition() {
		return currentX;
	}

	public float getYPosition() {
		return currentY;
	}

	public boolean moveUp() {
		if (getYPosition() < T * getZoom()) {
			setXYPosition(getXPosition(), getYPosition() + T / 2);
			return true;
		}
		return false;
	}

	public boolean moveDown() {
		if (getYPosition() > B * getZoom()) {
			setXYPosition(getXPosition(), getYPosition() + B / 2);
			return true;
		}
		return false;
	}

	public boolean moveLeft() {
		if (getXPosition() > L * getZoom()) {
			setXYPosition(getXPosition() + L / 2, getYPosition());
			return true;
		}
		return false;
	}

	public boolean moveRight() {
		if (getXPosition() < R * getZoom()) {
			setXYPosition(getXPosition() + R / 2, getYPosition());
			return true;
		}
		return false;
	}

	public boolean zoomIn() {
		float z = getZoom();
		if (z < 16.0f) {
			zoom(z * 2);
			return true;
		}
		return false;
	}

	public boolean zoomOut() {
		float z = getZoom();
		if (z > 1.0f / 16.0f) {
			zoom(z / 2);
			return true;
		}
		return false;
	}

	// Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// synchronized(slots){
		// if(stateAutoPlay != STATE_AUTOPLAY_IDLE)
		// return true;
		// }
		Log.i("GLSlideShowLayout", "onKeyDown(" + keyCode + ")");
		switch (keyCode) {
		case KeyEvent.KEYCODE_ENTER:
			// switchOrder(0);
			// switchSlot();
			break;

		case KeyEvent.KEYCODE_DPAD_UP:
			// moveUp();
			return true;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			// moveDown();
			return true;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			synchronized (slots) {
				String name = switchToPrevFile();
				if (name != null) {
					setMenuStatus("player_PAUSE");
					stopAutoPlay();
					filenamelistener.CallbackName("picture", name);
					filenamelistener.CallbackPosScale("picture", "Nothing");
					switchSlot();
				}
			}
			return true;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			synchronized (slots) {
				String name = switchToNextFile();
				if (name != null) {
					setMenuStatus("player_PAUSE");
					stopAutoPlay();
					filenamelistener.CallbackName("picture", name);
					filenamelistener.CallbackPosScale("picture", "Nothing");
					switchSlot();
				}
			}

			return true;
		}

		return false;
	}

	// Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	// Override
	public void setSlotsLayout() {
		// TODO Auto-generated method stub
		// resize and relocate slots

	}

	// private int drawTimes = 0;
	// Override
	public void drawFrame(GL10 gl) {
		// TODO Auto-generated method stub
		gl.glPushMatrix();
		setCamera(gl);

		synchronized (slots) {
			if (stateAutoPlay == STATE_AUTOPLAY_LOADTEXTURE) {
				// synchronized(slots){
				for (DisplaySlot slot : slots) {
					slot.LoadTextures(gl);
				}
				// }

				stateAutoPlay = STATE_AUTOPLAY_IDLE;
				lastTextureTim = System.currentTimeMillis();
				switchSlot();

				// Message message = new Message();
				// message.what = 2;
				// handlerAutoPlay.sendMessageDelayed(message, 100);
			}

			drawSlot(gl);

			if (autoPlayflag == true) {
				postNextAutoPlayMsg();
			}
		}

		drawEffects(gl);
		// drawTimes = (drawTimes+1) % 10000;
		gl.glPopMatrix();
	}

	/*
	 * //warning: this function can't be called in GL thread. private void
	 * waitDrawFrameTimes(int times){ if(times <= 0){ return; } int tm =
	 * drawTimes; while((10000 + drawTimes - tm)%10000 < times){ try {
	 * Thread.sleep(10); } catch (InterruptedException e) { //
	 * e.printStackTrace(); } } }
	 */
	/*
	 * //Override public void delLayoutTextures() { // TODO Auto-generated
	 * method stub //Only remove textures from GPU memory... for(DisplaySlot
	 * slot: slots){ slot.delSlotTextures(); } }
	 */

	public void CallbackMenuState(String... State) {
		synchronized (slots) {

			if (State[0].equals("BackTo3D")) {
				if (filenamelistener != null)
					filenamelistener.BackTo3D();
				return;
			}

			if (!State[0].equals("Picture"))
				return;
			Log.i("GLSlideShowLayout", "CallbackMenuState( " + State[1] + " )");

			if (State[1].equals("shortcut_common_pause_")) {
				startAutoPlay();
				String playmode = this.myConProvider.getMyParam("PicPlayMode");
				if (playmode.equals("HAND")) {
					this.myConProvider.setMyParam("PicPlayMode", "10S");
					delayAutoPlay = 10000;
				}
			} else if (State[1].equals("shortcut_common_play_")) {				
				stopAutoPlay();
			}
			if (State[1].equals("shortcut_common_stop_")) {
				if (filenamelistener != null) {
					filenamelistener.stopplayer("Picture");
				}
			} else if (State[1].equals("shortcut_picture_prev_")) {
				// switchOrder(0);
				String name = switchToPrevFile();
				if (name != null) {
					setMenuStatus("player_PAUSE");
					stopAutoPlay();
					filenamelistener.CallbackName("picture", name);
					filenamelistener.CallbackPosScale("picture", "Nothing");
					switchSlot();
				}
			} else if (State[1].equals("shortcut_picture_next_")) {
				// switchOrder(0);
				String name = switchToNextFile();
				if (name != null) {
					setMenuStatus("player_PAUSE");
					stopAutoPlay();
					filenamelistener.CallbackName("picture", name);
					filenamelistener.CallbackPosScale("picture", "Nothing");
					switchSlot();
				}

			} else if (State[1].equals("shortcut_common_playmode_normal")) {
				this.dataProvider.setSwitchMode(DataProvider.playmode_normal);
			} else if (State[1].equals("shortcut_common_playmode_folder")) {
				this.dataProvider.setSwitchMode(DataProvider.playmode_folder);
			} else if (State[1].equals("shortcut_common_playmode_rand")) {
				this.dataProvider.setSwitchMode(DataProvider.playmode_rand);
			} else if (State[1].equals("shortcut_picture_switch_time_3s")) {
				setMenuStatus("player_PLAY");
				delayAutoPlay = 3000;
				startAutoPlay();
			} else if (State[1].equals("shortcut_picture_switch_time_5s")) {
				setMenuStatus("player_PLAY");
				delayAutoPlay = 5000;
				startAutoPlay();
			} else if (State[1].equals("shortcut_picture_switch_time_10s")) {
				setMenuStatus("player_PLAY");
				delayAutoPlay = 10000;
				startAutoPlay();
			} else if (State[1].equals("shortcut_picture_switch_time_hand")) {
				setMenuStatus("player_PAUSE");
				stopAutoPlay();
			} else if (State[1].equals("shortcut_picture_rotate_normal")) // Õý³£
			{
				setMenuStatus("player_PAUSE");
				stopAutoPlay();
				rotate(0);
			} else if (State[1].equals("shortcut_picture_rotate_90"))// 90¶È
			{
				setMenuStatus("player_PAUSE");
				stopAutoPlay();
				rotate(90);
			} else if (State[1].equals("shortcut_picture_rotate_180"))// 180¶È
			{
				setMenuStatus("player_PAUSE");
				stopAutoPlay();
				rotate(180);
			} else if (State[1].equals("shortcut_picture_rotate_270"))// 270¶È
			{
				setMenuStatus("player_PAUSE");
				stopAutoPlay();
				rotate(270);
			} else if (State[1].equals("shortcut_common_zoom_big"))// ·Å´ó
			{
				setMenuStatus("player_PAUSE");
				stopAutoPlay();
				zoomIn();
			} else if (State[1].equals("shortcut_common_zoom_small"))// ·Å´ó
			{
				setMenuStatus("player_PAUSE");
				stopAutoPlay();
				zoomOut();
			} else if (State[1].equals("shortcut_picture_switch_mode_1"))// ÌØÐ§1
			{
				setSwitchMode(0);
				setDisappearColorMode(0);
				setAppearColorMode(1);

			} else if (State[1].equals("shortcut_picture_switch_mode_2"))// ÌØÐ§2
			{
				setSwitchMode(-1);
				setDisappearColorMode(-1);
				setAppearColorMode(-1);

			} else if (State[1].equals("shortcut_common_sync_play_music")) {
				if (filenamelistener != null) {
					filenamelistener.CallbackRelevance("Picture", "Audio");
				}
			} else if (State[1].equals("shortcut_common_sync_play_picture")) {
				if (filenamelistener != null) {
					filenamelistener.CallbackRelevance("Picture", "Picture");
				}

			} else if (State[1].equals("shortcut_common_sync_play_txt")) {
				if (filenamelistener != null) {
					filenamelistener.CallbackRelevance("Picture", "Text");
				}
			} else if (State[1].equals("shortcut_common_sync_control_picture")) {
				if (filenamelistener != null) {
					filenamelistener.CallbackName("picture", "Nothing");
					filenamelistener.CallbackPosScale("picture", "Nothing");
				}
			}
		}// end synchronized(slots)
	}

	private int delayAutoPlay = 12000;
	// private Timer timerAutoPlay = null;
	private boolean autoPlayflag = false;
	private long lastAutoPlayTim = 0;
	private long lastTextureTim = 0;
	private int stateAutoPlay = STATE_AUTOPLAY_IDLE;
	final static int STATE_AUTOPLAY_IDLE = 0;
	final static int STATE_AUTOPLAY_LOADTEXTURE = 1;

	private Handler handlerAutoPlay = new Handler() {
		// Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				synchronized (slots) {
					if (autoPlayflag == true) {
						String name = switchToNextFile();
						if (name != null) {
							filenamelistener.CallbackName("picture", name);
							filenamelistener.CallbackPosScale("picture",
									"Nothing");
							stateAutoPlay = STATE_AUTOPLAY_LOADTEXTURE;

						}
					}
				}
				break;

			// case 2:
			// synchronized(this){
			// switchSlot();
			// if(autoPlayflag == true){
			// postNextAutoPlayMsg();
			// }
			// }
			// break;
			}
			super.handleMessage(msg);
		}
	};

	/*
	 * TimerTask taskAutoPlay = new TimerTask(){ //Override public void run() {
	 * Message message = new Message(); message.what = 1;
	 * handlerAutoPlay.sendMessage(message); } };
	 */
	private void postNextAutoPlayMsg() {
		long tm = System.currentTimeMillis();
		if (delayAutoPlay + lastAutoPlayTim < tm
				&& lastTextureTim + SWITCH_TIMING + 500 < tm) {
			lastAutoPlayTim = tm;
			Message message = new Message();
			message.what = 1;
			handlerAutoPlay.sendMessageDelayed(message, 10);
		}
	}

	// Override
	public void startAutoPlay() {
		synchronized (slots) {
			if (autoPlayflag == false) {
				autoPlayflag = true;
				lastAutoPlayTim = System.currentTimeMillis();
				// postNextAutoPlayMsg();
			}
		}
		/*
		 * if(timerAutoPlay == null){ timerAutoPlay = new Timer();
		 * timerAutoPlay.schedule(taskAutoPlay, delayAutoPlay, delayAutoPlay);
		 * if(forceStopHandlerflag == false) { //show picture by staring in the
		 * first String name = switchToNextFile();
		 * filenamelistener.CallbackName("picture",name);
		 * filenamelistener.CallbackPosScale("picture","Nothing"); switchSlot();
		 * } }
		 */}

	// Override
	public void stopAutoPlay() {
		// if(timerAutoPlay != null){
		while (stateAutoPlay != STATE_AUTOPLAY_IDLE) { // wait for loadtexture.
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		synchronized (slots) {
			autoPlayflag = false;
		}

		// timerAutoPlay.cancel();
		// timerAutoPlay = null;
		// }
	}

	// Override
	public void onstop() {
		stopAutoPlay();
	}

	@SuppressWarnings("unchecked")
	private void setMenuStatus(String type) {
		List data = new ArrayList();
		if (type.equals("player_PLAY")) {
			data.add(playerStatus.player_PLAY);
			if (filenamelistener != null)
				filenamelistener.CallbackUpdataMenu("picture", data);
		} else if (type.equals("player_PAUSE")) {
			data.add(playerStatus.player_PAUSE);
			if (filenamelistener != null)
				filenamelistener.CallbackUpdataMenu("picture", data);

		}
	}
}
