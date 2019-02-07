import {getTreeFromFlatData} from '../component/ReactSortableTree/utils/tree-data-utils';
import {getReactSortableTreeDataFromArr} from "../util/viewDataUtil";

/**
 * 模拟表格模板侧边栏数据
 */
export function mockTableTemplateSidebarData() {

    const tbCatArr = ['Data Analysis', 'Calendars/Schedules', 'Budgets/Finance', 'Invoice/Order', 'HR',
        'Inventory', 'Project Management', 'Education', 'Form', 'Mortgage Calculators',
        'Exercise/Health', 'Lists/Checklists/Todo/Plan', 'Attendance', 'Travel/Itinerary', 'Note',
        'Geography/Map', 'Timesheet',];

    const dataAnalysisTableCategoryNameArr = ['Pie', 'Line', 'Bar', 'Point', 'Relationship', 'Heatmap', 'Radar',
        'Funnel', 'Boxplot', 'Pictorial', 'Candlestick', 'Gauge', 'Facet', ''];

    const flatData = [
        {tbTemplateName: 'Todo list', tbCat: tbCatArr[11],},
        {tbTemplateName: '2019 Calendar Blue', tbCat: tbCatArr[1],},
        {tbTemplateName: '2020 Calendar Green', tbCat: tbCatArr[1],},
        {tbTemplateName: 'Gantt Chart', tbCat: tbCatArr[6],},
        {tbTemplateName: 'Attendance', tbCat: tbCatArr[12],},
        {tbTemplateName: 'Weekly Report', tbCat: tbCatArr[16],},
        {tbTemplateName: 'Online Sales Tracker', tbCat: tbCatArr[0],},
        {tbTemplateName: 'Workout Log', tbCat: tbCatArr[10],},
        {tbTemplateName: 'Credit card tracker', tbCat: tbCatArr[2],},
        {tbTemplateName: 'Calendar Heatmap', tbCat: tbCatArr[0],},
        {tbTemplateName: 'Pareto Chart', tbCat: tbCatArr[0],},
        {tbTemplateName: 'Histogram', tbCat: tbCatArr[0],},
    ];

    const rootKey = -1;
    const argDefaults = {
        rootKey,
        getKey: node => node.tbTemplateName,
        getParentKey: node => node.tbCat,
    };

    // console.log(flatData)
    // const treeData = getTreeFromFlatData({...argDefaults, flatData});
    const treeData = getReactSortableTreeDataFromArr(flatData, "tbTemplateName", "tbCat");
    // console.log(treeData)

    return treeData;
}

