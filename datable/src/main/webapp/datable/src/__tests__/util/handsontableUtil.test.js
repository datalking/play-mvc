import { addPropsFor1stObjOfArray } from '../../util/handsontableUtil';

test('should add 26 props to 1st obj', () => {

    const arrOfObj = [
        { c1: 'c1', },
        { c1: 'c1', },
    ];

    const arrNew = addPropsFor1stObjOfArray(JSON.parse(JSON.stringify(arrOfObj)));

    // console.log('====测试addPropsFor1stObjOfArray')
    // console.log(arrOfObj)
    // console.log(arrNew)

    const addedPropsCount = Object.keys(arrNew[0]).length - Object.keys(arrOfObj[0]).length;

    expect(addedPropsCount).toBe(26);

});
