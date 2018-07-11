export function mockWorkbookDefaultData() {
    let tableArr = [];
    let rowNum = 64;
    let colNum = 28;

    if (colNum > (26 * 26)) {
        console.log("原始数据列数过多");
        return null;
    }

    for (let i = 0; i < rowNum; i++) {
        let arr = [];

        for (let j = 0; j < colNum; j++) {
            let content = j;
            let colHeaderPrefix = '';

            if (j > 25) {
                colHeaderPrefix = String.fromCharCode((j / 26)+64);
                content = (j % 25) - 1;
            }

            arr.push((i + 1) + '-' + colHeaderPrefix + String.fromCharCode(content + 65));
        }

        tableArr.push(arr);
    }

    return tableArr;
}
