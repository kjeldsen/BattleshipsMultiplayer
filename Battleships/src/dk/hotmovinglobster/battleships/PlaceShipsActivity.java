package dk.hotmovinglobster.battleships;

import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import dk.hotmovinglobster.battleships.BattleGrid.Point;
import dk.hotmovinglobster.battleships.BattleGrid.TileType;
import dk.hotmovinglobster.battleships.comm.CommunicationProtocolActivity;

public class PlaceShipsActivity extends CommunicationProtocolActivity implements BattleGridListener {

	private BattleGrid grid;
	private TextView txt_ships_remaining;
	
	private ProgressDialog dialog_waiting;
	private AlertDialog dialog_abort_warn;

	private Resources res;

	private int ships_remaining;
	
	private boolean isReady = false;
	private boolean opponentReady = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(GameContext.LOG_TAG, "PlaceShipsActivity.onCreate()");
		assert( GameContext.singleton.Comm != null );
		GameContext.singleton.Comm.setListener( this );
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.place_ships);

		new Handler();
		res = getResources();

		ships_remaining = GameContext.singleton.MAX_SHIPS;
		txt_ships_remaining = (TextView) findViewById(R.id.place_ships_txt_ships_remaining);
		updateShipsRemainingLabel();

		grid = new BattleGrid(this, GameContext.singleton.GRID_COLUMNS, GameContext.singleton.GRID_ROWS);
		grid.setListener(this);
		((FrameLayout) findViewById(R.id.place_ships_grid_frame)).addView(grid);
	}

	@Override
	public void onTileHit(int column, int row) {
		Log.v(GameContext.LOG_TAG, "PlaceShipsActivity.onTileHit(" + column + ", " + row + ")");
		if (ships_remaining > 0) {
			grid.setTileType(column, row, TileType.SHIP);
			updateShipsRemaining();
		}
	}

	private void updateShipsRemaining() {
		List<Point> ships = grid.getPointsWithType( TileType.SHIP );
		
		ships_remaining = GameContext.singleton.MAX_SHIPS - ships.size();
		
		updateShipsRemainingLabel();
		
		if (ships_remaining == 0) {
			allShipsPlaced();
		}
	}

	private void allShipsPlaced() {
		Log.i(GameContext.LOG_TAG, "PlaceShipsActivity: All ships placed");
		isReady = true;
		List<Point> ships = grid.getPointsWithType( TileType.SHIP );
		GameContext.singleton.Comm.sendShipsPlaced( ships );
		if (opponentReady) {
			bothReady();
		} else {
			showWaitingDialog();
		}
	}
	
	private void opponentReady() {
		opponentReady = true;
		Log.i(GameContext.LOG_TAG, "PlaceShipsActivity: Opponent ready");
		if (dialog_abort_warn != null && dialog_abort_warn.isShowing()) {
			dialog_abort_warn.dismiss();
		}
		if (dialog_waiting != null && dialog_waiting.isShowing()) {
			dialog_waiting.dismiss();
		}
		if (isReady) {
			bothReady();
		}
	}

	private void bothReady() {
		Toast.makeText(this, "Both players ready!", Toast.LENGTH_SHORT).show();
		finish();
	}

	private void showWaitingDialog() {
		final DialogInterface.OnClickListener dialog_click_listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
		        switch (which){
		        case DialogInterface.BUTTON_POSITIVE:
					dialog.dismiss();
					PlaceShipsActivity.this.finish();
		            break;

		        case DialogInterface.BUTTON_NEGATIVE:
		            showWaitingDialog();
		            break;
		        }		
		    }
		};
		
		dialog_waiting = new ProgressDialog(this);
		dialog_waiting.setIndeterminate(true);
		dialog_waiting.setMessage(res.getString(R.string.place_ships_all_ships_placed));
		dialog_waiting.setCancelable(true);
		dialog_waiting.show();
		dialog_waiting.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				dialog_waiting.dismiss();
				dialog_abort_warn = new AlertDialog.Builder(PlaceShipsActivity.this).setMessage(R.string.place_ships_warn_abort_wait).
				setPositiveButton(android.R.string.yes, dialog_click_listener).setNegativeButton(android.R.string.no, dialog_click_listener).show();
			}
		});
	}

	private void updateShipsRemainingLabel() {
		txt_ships_remaining.setText( res.getString( R.string.place_ships_remaining_ships_formatted, ships_remaining ) );
	}
	
	@Override
	public void communicationDisconnected() {
		Toast.makeText(this, "Disconnected from opponent (HC)", Toast.LENGTH_LONG).show();
		finish();
	}
	
	@Override
	public void communicationShipsPlaced(List<Point> ships) {
		Log.v(GameContext.LOG_TAG, "PlaceShipsActivity: Ships placed (amount: "+ships.size()+")");
		String places = "";
		for (Point p: ships) {
			places += p + ", ";
		}
		Log.v(GameContext.LOG_TAG, "PlaceShipsActivity: "+places);
		opponentReady();
	}

	@Override
	public void onBackPressed() {
		GameContext.singleton.Comm.disconnect();
	}
}
