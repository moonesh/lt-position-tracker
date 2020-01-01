package com.lt.tracker.data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GlobalPosition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Repository;

import com.lt.tracker.domain.VehicleBuilder;
import com.lt.tracker.domain.VehicleNotFoundException;
import com.lt.tracker.domain.VehiclePosition;

/**
 * This is a very quick and dirty implementation of a Mongo data store. The implementations
 * are certainly sub-optimal. This may be improved in a future release, but the purpose of 
 * this was to demonstrate how mongo or similar persistent stores can be deployed
 * to a K8S cluster.
 */
@Repository
public class DataMongoDbImpl implements Data {
	private static final BigDecimal MPS_TO_MPH_FACTOR = new BigDecimal("2.236936");
	private GeodeticCalculator geoCalc = new GeodeticCalculator();

	@Autowired
	private PositionRepository mongoDb;
	
	@Override
	public void updatePosition(VehiclePosition position) {
		
		BigDecimal speed =null;
		try{
		 speed = calculateSpeedInMph(position.getName(),position);

		}
		catch(VehicleNotFoundException vnh){
			System.out.println("Initialising position for : "+position.getName());
		}
		position=new VehicleBuilder().withVehiclePostion(position).withSpeed(speed).build();
		mongoDb.insert(position);
	}

	private BigDecimal calculateSpeedInMph(String vehicleName, VehiclePosition newPosition) throws VehicleNotFoundException
	{		
	//	TreeSet<VehiclePosition> positions = positionDatabase.get(vehicleName);
		//if (positions.isEmpty()) return null;
		
		VehiclePosition posB = newPosition;
		VehiclePosition posA = getLatestPositionFor(vehicleName); // confusing - this is actually the last report recorded
		
		long timeAinMillis = posA.getTimestamp().getTime();
		long timeBinMillis = posB.getTimestamp().getTime();
		long timeInMillis = timeBinMillis - timeAinMillis;
		if (timeInMillis == 0) return new BigDecimal("0");
		
		BigDecimal timeInSeconds = new BigDecimal(timeInMillis / 1000.0);
				
		GlobalPosition pointA = new GlobalPosition(posA.getLat().doubleValue(), posA.getLongitude().doubleValue(), 0.0);
		GlobalPosition pointB = new GlobalPosition(posB.getLat().doubleValue(), posB.getLongitude().doubleValue(), 0.0);
	
		double distance = geoCalc.calculateGeodeticCurve(Ellipsoid.WGS84, pointA, pointB).getEllipsoidalDistance(); // Distance between Point A and Point B
		BigDecimal distanceInMetres = new BigDecimal (""+ distance);
		
		BigDecimal speedInMps = distanceInMetres.divide(timeInSeconds, RoundingMode.HALF_UP);
		BigDecimal milesPerHour = speedInMps.multiply(MPS_TO_MPH_FACTOR);
		return milesPerHour;
	}

	
	@Override
	public VehiclePosition getLatestPositionFor(String vehicleName) throws VehicleNotFoundException {
		// A *very* basic implementation!
		Example<VehiclePosition> example = Example.of(new VehicleBuilder().withName(vehicleName).build());
		List<VehiclePosition> all = mongoDb.findAll(example);
		if (all.size() == 0) throw new VehicleNotFoundException();
		return all.get(all.size() - 1);
	}

	@Override
	public void addAllReports(VehiclePosition[] allReports) {
		for (VehiclePosition next: allReports)
		{
			this.updatePosition(next);
		}
	}

	@Override
	public Collection<VehiclePosition> getLatestPositionsOfAllVehiclesUpdatedSince(Date since) {
		return mongoDb.findByTimestampAfter(since);
	}

	@Override
	public TreeSet<VehiclePosition> getAllReportsForVehicleSince(String name, Date timestamp)
			throws VehicleNotFoundException {
		return new TreeSet<VehiclePosition>(mongoDb.findByNameAndTimestampAfter(name, timestamp));
	}

	@Override
	public Collection<VehiclePosition> getHistoryFor(String vehicleName) throws VehicleNotFoundException {
		VehiclePosition position = new VehicleBuilder().withName(vehicleName).build();
		Example<VehiclePosition> example = Example.of(position);
		return new TreeSet<VehiclePosition>(mongoDb.findAll(example)); // just a hack to sort correctly
	}

}
