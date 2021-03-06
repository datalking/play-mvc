import {UPDATE_SHEET_DATA, UPDATE_SHEET_READ_ONLY} from "../common/constant/actionTypeConstant";

export const updateSheetData = (changes, source) => ({
    type: UPDATE_SHEET_DATA,
    dataChanges: changes,
});

export const updateSheetReadOnly = (checked) => ({
    type: UPDATE_SHEET_READ_ONLY,
    readOnly: checked,
});
