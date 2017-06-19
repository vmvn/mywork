import React from 'react';
import {Form, Select} from 'antd';
const FormItem = Form.Item;
const Option = Select.Option;

import FormGroup from './base/FormGroup';
import FormItem4Title from './base/FormItem4Title';
import FormItem4ShowNum from './base/FormItem4ShowNum';
import FormItem4LinkMode from './base/FormItem4LinkMode';
import FormItem4Field from './base/FormItem4Field';
import FormItem4RemindMode from './base/FormItem4RemindMode';
import FormGroup4DataSource from './base/FormGroup4DataSource';

// 流程中心
export default class Content_8 extends React.Component {
    render() {
        const {eid, ebaseid, eShareLevel, eContent} = this.props.data;
        const {eScrollDirection, eTabs} = eContent;
        const {getFieldProps} = this.props.form;
        const {formItemLayout} = this.props;

        let eFormItem4ScrollDirection = <div></div>;
        let formGroup4DataSource = <div></div>;
        if (eShareLevel == '2') {
            const eScrollDirectionProps = getFieldProps('eContentScrollDirection', {initialValue: eScrollDirection.selected});
            const eScrollDirectionOptions = eScrollDirection.options.map((item, index) => {
                return <Option key={index} value={item.key}>{item.value}</Option>;
            });

            eFormItem4ScrollDirection = (
                <FormItem label="滚动方向" {...formItemLayout}>
                    <Select size="default" style={{width: '80px'}} {...eScrollDirectionProps}>
                        {eScrollDirectionOptions}
                    </Select>
                </FormItem>
            );

            const props = {
                eid: eid,
                ebaseid: ebaseid,
                eTabs: eTabs,
                baseForm: this.props.form,
                formItemLayout: formItemLayout
            };
            formGroup4DataSource = <FormGroup4DataSource {...props}/>;
        }

        return (
            <div>
                <Form horizontal className="esetting-form">
                    <FormGroup title="基本信息">
                        <FormItem4Title {...this.props}/>
                        <FormItem4ShowNum {...this.props}/>
                        <FormItem4LinkMode {...this.props}/>
                        <FormItem4Field {...this.props}/>
                        {eFormItem4ScrollDirection}
                        <FormItem4RemindMode {...this.props}/>
                    </FormGroup>
                </Form>
                {formGroup4DataSource}
            </div>
        );
    }
}