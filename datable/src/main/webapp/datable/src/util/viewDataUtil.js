/**
 * 将输入数组分成等长的子数组
 * @param arr 原数组
 * @param size 每组个数
 */
export function sliceArr(arr, size) {

    if (arr === undefined || arr === null || arr.length < 1) {
        console.log('error, arr cannot be null or empty', arr + '');
    }

    const resultArr = [];
    const arrLen = arr.length;

    if (arrLen < 6) {
        resultArr.push(arr);
    }

    const subArrCount = (arrLen % size === 0) ? (arrLen / size) : (Math.floor(arrLen / size) + 1);
    // console.log(subArrCount)

    for (let i = 0; i < subArrCount; i++) {

        const subArr = [];
        for (let j = 0; j < 5 && (i * 5 + j < arrLen); j++) {
            subArr.push(arr[i * 5 + j])
        }

        resultArr.push(subArr);
    }

    return resultArr;
}

/**
 * 将输入的对象数组根据一个属性分组另一个属性，返回树形结构的数据给react-sortable-tree使用
 */
export function getReactSortableTreeDataFromArr(arr, tbFileName, tbCategoryName,) {
    if (arr === undefined || arr === null || arr.length < 1) {
        console.log('error, arr cannot be null or empty', arr + '');
    }

    const resultTreeData = [];

    // 存储所有类别名
    const catNames = [];
    // 二维数组，存储各类别对应的文件名
    const catFileNames = [];

    for (let i = 0; i < arr.length; i++) {

        const obj = arr[i];
        const oCatName = obj[tbCategoryName];
        const oFileName = obj[tbFileName];
        // console.log(obj)

        if (catNames.indexOf(oCatName) === -1) {
            catNames.push(oCatName);
        }
        // console.log(catNames)

        const catIndex = catNames.indexOf(oCatName);

        // console.log(catIndex)

        if (catFileNames[catIndex] === undefined) {
            const fileNameArr = [];
            fileNameArr.push(oFileName);
            catFileNames[catIndex] = fileNameArr;
        } else if (catFileNames[catIndex] !== undefined && catFileNames[catIndex] instanceof Array) {
            if (catFileNames[catIndex].indexOf(oFileName) === -1) {
                catFileNames[catIndex].push(oFileName);
            }
        }

    }

    // console.log(catNames)
    // console.log(catFileNames)

    for (let j = 0; j < catNames.length; j++) {

        const curFileNameArr = catFileNames[j];
        const subTreeNode = [];

        for (let k = 0; k < curFileNameArr.length; k++) {

            const leafNode = {
                title: curFileNameArr[k],
                // expanded: true,
            };
            subTreeNode.push(leafNode);
        }

        // 顶层不展开
        const curTreeNode = {
            title: catNames[j],
            expanded: false,
            children: subTreeNode,
        };

        resultTreeData.push(curTreeNode);
    }

    return resultTreeData;
}