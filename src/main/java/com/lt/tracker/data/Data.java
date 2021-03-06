package com.lt.tracker.data;

import java.util.Collection;
import java.util.Date;
import java.util.TreeSet;

import com.lt.tracker.domain.VehicleNotFoundException;
import com.lt.tracker.domain.VehiclePosition;

public interface Data {

	void updatePosition(VehiclePosition position);

	VehiclePosition getLatestPositionFor(String vehicleName) throws VehicleNotFoundException;

	void addAllReports(VehiclePosition[] allReports);

	Collection<VehiclePosition> getLatestPositionsOfAllVehiclesUpdatedSince(Date since);

	TreeSet<VehiclePosition> getAllReportsForVehicleSince(String name, Date timestamp) throws VehicleNotFoundException;

	Collection<VehiclePosition> getHistoryFor(String vehicleName) throws VehicleNotFoundException;
}