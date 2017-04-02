package auction.commands;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;
import java.util.Map;

import auction.Command;

public class ReadAuctionCmd extends Command implements Runnable {

	@Override
	public StringBuffer execute(Connection connection,
			Map<String, Object> mapUserData) throws Exception {
		StringBuffer strbufResult;
		CallableStatement sqlProc;
		int pUID, pID;
		
		pUID = (int) mapUserData.get("pUID");
		pID = (int) mapUserData.get("pID");
		
		sqlProc = connection.prepareCall("{?=call getAuctionForUser(?,?)}");
		sqlProc.registerOutParameter(1, Types.INTEGER);
		sqlProc.setInt(2, pUID);
		sqlProc.setInt(3, pID);
		
		sqlProc.execute();
		strbufResult = makeJSONResponseEnvelope(sqlProc.getInt(1), null, null);
		sqlProc.close();

		return strbufResult;
	}

}
