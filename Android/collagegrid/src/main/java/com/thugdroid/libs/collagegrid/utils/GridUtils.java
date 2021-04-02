package com.thugdroid.libs.collagegrid.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;

import com.thugdroid.libs.collagegrid.R;
import com.thugdroid.libs.collagegrid.constants.GridNameConstants;
import com.thugdroid.libs.collagegrid.model.GridConfig;

public class GridUtils {
    private static int INCREMENT_FOR_HEIGHT_BASED_GRIDS=25;
    public static String[] getGridNames(){
        String[] fileNames={GridNameConstants.L1,
                GridNameConstants.L2,GridNameConstants.L3,
                GridNameConstants.L4,GridNameConstants.L5,
                GridNameConstants.L6,GridNameConstants.L7,
                GridNameConstants.L8,GridNameConstants.L9,
                GridNameConstants.L10,GridNameConstants.L11,
                GridNameConstants.L12,GridNameConstants.L13,
                GridNameConstants.L14,GridNameConstants.L15,
                GridNameConstants.L16,GridNameConstants.L17,
                GridNameConstants.L18,GridNameConstants.L19,
                GridNameConstants.L20
        };
        return fileNames;
    }

    public static GridConfig getGridConfig(Context context,String gridName, int displayWidth){
        GridConfig gridConfig=new GridConfig();
        int halfWidth = displayWidth/2;
        int oneThirdOfWidth =displayWidth/3;
        int heightBasedIncrements = (INCREMENT_FOR_HEIGHT_BASED_GRIDS*(context.getResources().getDisplayMetrics().densityDpi/ DisplayMetrics.DENSITY_DEFAULT));
        if(gridName==null){
            gridName=GridNameConstants.L1;
        }
        switch (gridName){
                case GridNameConstants.L2:
                    gridConfig.setLayout(R.layout.ab);
                    gridConfig.setLayoutCount(2);
                    gridConfig.setGrid1Width(displayWidth);
                    gridConfig.setGrid1Height(halfWidth);
                    gridConfig.setGrid2Width(displayWidth);
                    gridConfig.setGrid2Height(halfWidth);
                    break;
            case GridNameConstants.L3:
                gridConfig.setLayout(R.layout.ac);
                gridConfig.setLayoutCount(3);
                gridConfig.setGrid1Width(displayWidth);
                gridConfig.setGrid1Height(oneThirdOfWidth+heightBasedIncrements);
                gridConfig.setGrid2Width(displayWidth);
                gridConfig.setGrid2Height(oneThirdOfWidth+heightBasedIncrements);
                gridConfig.setGrid3Width(displayWidth);
                gridConfig.setGrid3Height(oneThirdOfWidth+heightBasedIncrements);
                break;
            case GridNameConstants.L4:
                gridConfig.setLayout(R.layout.ad);
                gridConfig.setLayoutCount(4);
                gridConfig.setGrid1Width(halfWidth);
                gridConfig.setGrid1Height(halfWidth);
                gridConfig.setGrid2Width(halfWidth);
                gridConfig.setGrid2Height(halfWidth);
                gridConfig.setGrid3Width(halfWidth);
                gridConfig.setGrid3Height(halfWidth);
                gridConfig.setGrid4Width(halfWidth);
                gridConfig.setGrid4Height(halfWidth);

                break;
            case GridNameConstants.L5:
                gridConfig.setLayout(R.layout.ae);
                gridConfig.setLayoutCount(6);
                gridConfig.setGrid1Width(halfWidth);
                gridConfig.setGrid1Height(oneThirdOfWidth+heightBasedIncrements);
                gridConfig.setGrid2Width(halfWidth);
                gridConfig.setGrid2Height(oneThirdOfWidth+heightBasedIncrements);
                gridConfig.setGrid3Width(halfWidth);
                gridConfig.setGrid3Height(oneThirdOfWidth+heightBasedIncrements);
                gridConfig.setGrid4Width(halfWidth);
                gridConfig.setGrid4Height(oneThirdOfWidth+heightBasedIncrements);
                gridConfig.setGrid5Width(halfWidth);
                gridConfig.setGrid5Height(oneThirdOfWidth+heightBasedIncrements);
                gridConfig.setGrid6Width(halfWidth);
                gridConfig.setGrid6Height(oneThirdOfWidth+heightBasedIncrements);

                break;
            case GridNameConstants.L6:
                gridConfig.setLayout(R.layout.af);
                gridConfig.setLayoutCount(6);
                gridConfig.setGrid1Width(oneThirdOfWidth);
                gridConfig.setGrid1Height(halfWidth);
                gridConfig.setGrid2Width(oneThirdOfWidth);
                gridConfig.setGrid2Height(halfWidth);
                gridConfig.setGrid3Width(oneThirdOfWidth);
                gridConfig.setGrid3Height(halfWidth);
                gridConfig.setGrid4Width(oneThirdOfWidth);
                gridConfig.setGrid4Height(halfWidth);
                gridConfig.setGrid5Width(oneThirdOfWidth);
                gridConfig.setGrid5Height(halfWidth);
                gridConfig.setGrid6Width(oneThirdOfWidth);
                gridConfig.setGrid6Height(halfWidth);

                break;
            case GridNameConstants.L7:
                gridConfig.setLayout(R.layout.ag);
                gridConfig.setLayoutCount(2);
                gridConfig.setGrid1Width(halfWidth);
                gridConfig.setGrid1Height(displayWidth);
                gridConfig.setGrid2Width(halfWidth);
                gridConfig.setGrid2Height(displayWidth);
                break;
            case GridNameConstants.L8:
                gridConfig.setLayout(R.layout.ah);
                gridConfig.setLayoutCount(3);
                gridConfig.setGrid1Width(halfWidth);
                gridConfig.setGrid1Height(halfWidth);
                gridConfig.setGrid2Width(halfWidth);
                gridConfig.setGrid2Height(halfWidth);
                gridConfig.setGrid3Width(displayWidth);
                gridConfig.setGrid3Height(halfWidth);

                break;
            case GridNameConstants.L9:
                gridConfig.setLayout(R.layout.ai);
                gridConfig.setLayoutCount(3);
                gridConfig.setGrid1Width(displayWidth);
                gridConfig.setGrid1Height(halfWidth);
                gridConfig.setGrid2Width(halfWidth);
                gridConfig.setGrid2Height(halfWidth);
                gridConfig.setGrid3Width(halfWidth);
                gridConfig.setGrid3Height(halfWidth);

                break;
            case GridNameConstants.L10:
                gridConfig.setLayout(R.layout.aj);
                gridConfig.setLayoutCount(3);
                gridConfig.setGrid1Width(halfWidth);
                gridConfig.setGrid1Height(halfWidth);
                gridConfig.setGrid2Width(halfWidth);
                gridConfig.setGrid2Height(displayWidth);
                gridConfig.setGrid3Width(halfWidth);
                gridConfig.setGrid3Height(halfWidth);

                break;
            case GridNameConstants.L11:
                gridConfig.setLayout(R.layout.ak);
                gridConfig.setLayoutCount(3);
                gridConfig.setGrid1Width(halfWidth);
                gridConfig.setGrid1Height(displayWidth);
                gridConfig.setGrid2Width(halfWidth);
                gridConfig.setGrid2Height(halfWidth);
                gridConfig.setGrid3Width(halfWidth);
                gridConfig.setGrid3Height(halfWidth);

                break;
            case GridNameConstants.L12:
                gridConfig.setLayout(R.layout.al);
                gridConfig.setLayoutCount(4);
                gridConfig.setGrid1Width(oneThirdOfWidth);
                gridConfig.setGrid1Height(halfWidth);
                gridConfig.setGrid2Width(oneThirdOfWidth);
                gridConfig.setGrid2Height(halfWidth);
                gridConfig.setGrid3Width(oneThirdOfWidth);
                gridConfig.setGrid3Height(halfWidth);
                gridConfig.setGrid4Width(displayWidth);
                gridConfig.setGrid4Height(halfWidth);

                break;
            case GridNameConstants.L13:
                gridConfig.setLayout(R.layout.am);
                gridConfig.setLayoutCount(4);
                gridConfig.setGrid1Width(displayWidth);
                gridConfig.setGrid1Height(halfWidth);
                gridConfig.setGrid2Width(oneThirdOfWidth);
                gridConfig.setGrid2Height(halfWidth);
                gridConfig.setGrid3Width(oneThirdOfWidth);
                gridConfig.setGrid3Height(halfWidth);
                gridConfig.setGrid4Width(oneThirdOfWidth);
                gridConfig.setGrid4Height(halfWidth);

                break;
            case GridNameConstants.L14:
                gridConfig.setLayout(R.layout.an);
                gridConfig.setLayoutCount(4);
                gridConfig.setGrid1Width(halfWidth);
                gridConfig.setGrid1Height(oneThirdOfWidth);
                gridConfig.setGrid2Width(halfWidth);
                gridConfig.setGrid2Height(displayWidth);
                gridConfig.setGrid3Width(halfWidth);
                gridConfig.setGrid3Height(oneThirdOfWidth);
                gridConfig.setGrid4Width(halfWidth);
                gridConfig.setGrid4Height(oneThirdOfWidth);

                break;

            case GridNameConstants.L15:
                gridConfig.setLayout(R.layout.ao);
                gridConfig.setLayoutCount(4);
                gridConfig.setGrid1Width(halfWidth);
                gridConfig.setGrid1Height(displayWidth);
                gridConfig.setGrid2Width(halfWidth);
                gridConfig.setGrid2Height(oneThirdOfWidth);
                gridConfig.setGrid3Width(halfWidth);
                gridConfig.setGrid3Height(oneThirdOfWidth);
                gridConfig.setGrid4Width(halfWidth);
                gridConfig.setGrid4Height(oneThirdOfWidth);

                break;
            case GridNameConstants.L16:
                gridConfig.setLayout(R.layout.ap);
                gridConfig.setLayoutCount(5);
                gridConfig.setGrid1Width(oneThirdOfWidth);
                gridConfig.setGrid1Height(halfWidth);
                gridConfig.setGrid2Width(oneThirdOfWidth);
                gridConfig.setGrid2Height(halfWidth);
                gridConfig.setGrid3Width(oneThirdOfWidth);
                gridConfig.setGrid3Height(halfWidth);
                gridConfig.setGrid4Width(halfWidth);
                gridConfig.setGrid4Height(halfWidth);
                gridConfig.setGrid5Width(halfWidth);
                gridConfig.setGrid5Height(halfWidth);

                break;
            case GridNameConstants.L17:
                gridConfig.setLayout(R.layout.aq);
                gridConfig.setLayoutCount(5);
                gridConfig.setGrid1Width(halfWidth);
                gridConfig.setGrid1Height(oneThirdOfWidth);
                gridConfig.setGrid2Width(halfWidth);
                gridConfig.setGrid2Height(halfWidth);
                gridConfig.setGrid3Width(halfWidth);
                gridConfig.setGrid3Height(oneThirdOfWidth);
                gridConfig.setGrid4Width(halfWidth);
                gridConfig.setGrid4Height(halfWidth);
                gridConfig.setGrid5Width(halfWidth);
                gridConfig.setGrid5Height(oneThirdOfWidth);


                break;
            case GridNameConstants.L18:
                gridConfig.setLayout(R.layout.ar);
                gridConfig.setLayoutCount(5);
                gridConfig.setGrid1Width(halfWidth);
                gridConfig.setGrid1Height(oneThirdOfWidth+heightBasedIncrements);
                gridConfig.setGrid2Width(halfWidth);
                gridConfig.setGrid2Height(oneThirdOfWidth+heightBasedIncrements);
                gridConfig.setGrid3Width(halfWidth);
                gridConfig.setGrid3Height(oneThirdOfWidth+heightBasedIncrements);
                gridConfig.setGrid4Width(halfWidth);
                gridConfig.setGrid4Height(oneThirdOfWidth+heightBasedIncrements);
                gridConfig.setGrid5Width(displayWidth);
                gridConfig.setGrid5Height(oneThirdOfWidth+heightBasedIncrements);

                break;
            case GridNameConstants.L19:
                gridConfig.setLayout(R.layout.as);
                gridConfig.setLayoutCount(5);
                gridConfig.setGrid1Width(displayWidth);
                gridConfig.setGrid1Height(oneThirdOfWidth+heightBasedIncrements);
                gridConfig.setGrid2Width(halfWidth);
                gridConfig.setGrid2Height(oneThirdOfWidth+heightBasedIncrements);
                gridConfig.setGrid3Width(halfWidth);
                gridConfig.setGrid3Height(oneThirdOfWidth+heightBasedIncrements);
                gridConfig.setGrid4Width(halfWidth);
                gridConfig.setGrid4Height(oneThirdOfWidth+heightBasedIncrements);
                gridConfig.setGrid5Width(halfWidth);
                gridConfig.setGrid5Height(oneThirdOfWidth+heightBasedIncrements);

                break;
            case GridNameConstants.L20:
                gridConfig.setLayout(R.layout.at);
                gridConfig.setLayoutCount(5);
                gridConfig.setGrid1Width(halfWidth);
                gridConfig.setGrid1Height(oneThirdOfWidth+heightBasedIncrements);
                gridConfig.setGrid2Width(halfWidth);
                gridConfig.setGrid2Height(oneThirdOfWidth+heightBasedIncrements);
                gridConfig.setGrid3Width(displayWidth);
                gridConfig.setGrid3Height(oneThirdOfWidth+heightBasedIncrements);
                gridConfig.setGrid4Width(halfWidth);
                gridConfig.setGrid4Height(oneThirdOfWidth+heightBasedIncrements);
                gridConfig.setGrid5Width(halfWidth);
                gridConfig.setGrid5Height(oneThirdOfWidth+heightBasedIncrements);

                break;
            default:
                gridConfig.setLayout(R.layout.aa);
                gridConfig.setLayoutCount(1);
                gridConfig.setGrid1Width(displayWidth);
                gridConfig.setGrid1Height(displayWidth);
                break;

        }
        return gridConfig;
    }

    public static int getGridId(int gridNo){
        switch (gridNo){
            case 2:
                return R.id.grid2;
            case 3:
                return R.id.grid3;
            case 4:
                return R.id.grid4;
            case 5:
                return R.id.grid5;
            case 6:
                return R.id.grid6;
            default:
                return R.id.grid1;
        }
    }
}
