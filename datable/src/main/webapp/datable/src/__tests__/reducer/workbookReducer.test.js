import wbookReducer from '../../reducer/workbookReducer';
import * as types from '../../action/actionTypeConstant';
import {mockWorkbookDefaultData} from "../../util/mockData";

describe('test workbookReducer', () => {

    const defaultState = {
        settings: {
            data: [],
            colHeaders: true,
            rowHeaders: true,
            readOnly: false,
            colWidths: 100,
            minSpareRows: 160,
            minSpareCols: 26,
        },
    };

    it('should handle update sheet read only', () => {

        const newState = wbookReducer(defaultState, {
            type: types.UPDATE_SHEET_READ_ONLY,
            readOnly: true,
        });

        const expectedState = {
            settings: {
                data: [],
                colHeaders: true,
                rowHeaders: true,
                readOnly: true,
                colWidths: 100,
                minSpareRows: 160,
                minSpareCols: 26,
            },
        };

        expect(newState).toEqual(expectedState);
        // expect(66).toEqual(66);
    });

});
