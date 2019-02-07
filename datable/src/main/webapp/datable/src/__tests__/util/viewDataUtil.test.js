import {sliceArr} from '../../util/viewDataUtil';

test('sliceArr - should split input arr to sub arr', () => {

    const arr1 = [11, 22, 33, 44, 55, 6, 7, 8, 9, 0];
    const arr2 = [11, 22, 33, 44, 55, 6, 7, 8, 9, 0, 1];

    const arrNew1 = sliceArr(JSON.parse(JSON.stringify(arr1)), 5);
    const arrNew2 = sliceArr(JSON.parse(JSON.stringify(arr2)), 5);

    // console.log(arrNew1);
    // console.log(arrNew2);

    const arrNew1Len = arrNew1.length;
    const arrNew2Len = arrNew2.length;

    expect(arrNew1Len).toBe(2);
    expect(arrNew2Len).toBe(3);

});
