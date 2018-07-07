package com.tongtong.purchaser.utils;

import java.util.ArrayList;
import java.util.List;

import com.tongtong.purchaser.model.RegionModel;




public class RegionUtil {
	public static List<RegionModel> getRegionStructure(
			List<RegionModel> oldRegions) {
		List<RegionModel> newRegions = new ArrayList<RegionModel>();
		for (int i = 0; i < oldRegions.size(); i++) {
			RegionModel region = oldRegions.get(i);
			if (region.getParentId() == -1) {
				newRegions.add(region);
			}
		}
		for (int i = 0; i < newRegions.size(); i++) {
			RegionModel region = newRegions.get(i);
			List<RegionModel> itemRegions = new ArrayList<RegionModel>();
			for (int j = 0; j < oldRegions.size(); j++) {

				if (oldRegions.get(j).getParentId() == region.getId()) {
					RegionModel itemRegion = oldRegions.get(j);
					List<RegionModel> subItemRegions = new ArrayList<RegionModel>();
					for (int k = 0; k < oldRegions.size(); k++) {
						if (oldRegions.get(k).getParentId() == itemRegion
								.getId()) {
							RegionModel subItemRegion = oldRegions.get(k);
							subItemRegions.add(subItemRegion);
						}
					}
					itemRegion.setChildrenRegions(subItemRegions);
					itemRegions.add(itemRegion);
				}
			}
			region.setChildrenRegions(itemRegions);
		}
        
		return newRegions;

	}
}
