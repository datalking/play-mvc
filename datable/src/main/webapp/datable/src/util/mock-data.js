/**
 * 模拟excel的默认数据
 */
export function mockWorkbookDefaultData(rolN, colN) {
    export function mockWorkbookDefaultDataArrayOfArray();
}

/**
 * 模拟excel的默认数据，使用二维数组
 */
export function mockWorkbookDefaultDataArrayOfArray(rolN, colN) {
    // 最终返回的数组
    let tableArr = [];
    let rowNum = rolN || 1000;
    let colNum = colN || 100;

    if (colNum > (26 * 26)) {
        console.log("原始数据列数过多");
        return null;
    }

    // 逐行填充数据
    for (let i = 0; i < rowNum; i++) {
        // 每一行数据构成的数组
        let arr = [];

        // 对该行逐列填充数据，行号-列头
        for (let j = 0; j < colNum; j++) {
            // 第一位默认为空
            let colHeaderPrefix = '';
            // 第二位从A-Z
            let content = j;

            if (j > 25) {
                //第一位从A-Z
                colHeaderPrefix = String.fromCharCode((j / 26) + 64);

                content = j % 26;
            }

            arr.push((i + 1) + '-' + colHeaderPrefix + String.fromCharCode(content + 65));
        }

        tableArr.push(arr);
    }

    return tableArr;
}
