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
{
    "msgtype": "markdown",
    "markdown": {
        "title":"升级报告",
        "text": "# ${jobName}升级${state}报告 \n ## 操作人:\n\n ${executionData.user} \n ## 项目: \n\n ${executionData.project} \n ## 任务:\n\n [${jobName}](${executionData.job.href}) \n ## 任务编号:\n\n [${executionData.id}](${executionData.href}) \n ## 执行参数:\n\n ${executionData.argstring} "
     }
}

