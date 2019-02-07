import * as actions from '../../action/workbookAction';
import * as types from '../../common/constant/actionTypeConstant';


describe('call workbookAction', () => {

    it('should create an action to update sheet data', () => {
        const dataChanges = 'mock input';
        const expectedAction = {
            type: types.UPDATE_SHEET_DATA,
            dataChanges,
        };

        //toEqual递归检查对象或者数组中的每个字段
        expect(actions.updateSheetData(dataChanges)).toEqual(expectedAction);

    });

});