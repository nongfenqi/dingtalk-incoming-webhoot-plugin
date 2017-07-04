<#if executionData.job.group??>
    <#assign jobName="${executionData.job.group} / ${executionData.job.name}">
<#else>
    <#assign jobName="${executionData.job.name}">
</#if>
<#assign message="<${executionData.href}|Execution #${executionData.id}> of job <${executionData.job.href}|${jobName}>">
<#if trigger == "start">
    <#assign state="开始">
<#elseif trigger == "failure">
    <#assign state="失败">
<#else>
    <#assign state="成功">
</#if>
<#assign optionStr = "">
<#list executionData.context.option?keys as key>
    <#assign optionStr = optionStr + "\\n* " + key + " = " + executionData.context.option[key]>
</#list>
{
    "msgtype": "markdown",
    "markdown": {
        "title":"${jobName}升级${state}报告",
        "text": "# ${jobName}升级${state}报告 \n ## 操&nbsp;&nbsp;作&nbsp;人: ${executionData.user} \n ## 项&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;目:  ${executionData.project} \n ## 任&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;务: [${jobName}](${executionData.job.href}) \n ## 任务编号: [${executionData.id}](${executionData.href}) \n ## 执行参数: ${optionStr} "
     }
}

