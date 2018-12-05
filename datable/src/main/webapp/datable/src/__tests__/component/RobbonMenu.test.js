import React from 'react';
import Enzyme, {mount} from 'enzyme';
import Adapter from 'enzyme-adapter-react-16';
import RibbonMenu from '../../component/RibbonMenu';

Enzyme.configure({adapter: new Adapter()});

function setup() {

    const props = {
        topMenuList: ["File", "Home", "Insert", "Data", "Review", "View", "Chart", "help2",],
        setSheetReadOnly: function () {
        },
        readOnlyState: false,
    };

    const enzymeWrapper = mount(<RibbonMenu {...props} />);

    return {
        props,
        enzymeWrapper,
    }
}

describe('ribbon menu components', () => {

    describe('ribbon menu', () => {

        it('should render self and subcomponents', () => {
            const {enzymeWrapper} = setup();

            // expect(enzymeWrapper.find('header').hasClass('header')).toBe(true)
            // expect(enzymeWrapper.find('h1').text()).toBe('todos')

            const readOnlyInputProps = enzymeWrapper.find('input').props();

            expect(readOnlyInputProps.id).toEqual("readOnlyCheck");
            // expect(todoInputProps.placeholder).toEqual('What needs to be done?')
        })

    })
});