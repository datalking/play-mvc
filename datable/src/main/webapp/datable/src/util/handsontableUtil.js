/**
 * 给对象数组的第一个对象添加属性
 */
export function addPropsFor1stObjOfArray(arrOfObj) {

    const isArrValid = (arrOfObj != undefined
        && arrOfObj != null
        && arrOfObj instanceof Array
        && arrOfObj.length > 0);

    if (isArrValid) {
        const firstObj = arrOfObj[0];
        for (let i = 0; i < 26; i++) {
            const kName = 'col-' + i;
            firstObj[kName] = '';
        }

    }

    return arrOfObj;
}