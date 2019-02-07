import {mockTableTemplateSidebarData} from '../../mock/mockComponentViewData';

test('mockTableTemplateSidebarData', () => {

    const data = mockTableTemplateSidebarData();

    // console.log(data);

    expect(data).not.toEqual(null);

});
