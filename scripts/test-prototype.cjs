// Optional browser checks and reproducible prototype captures. Requires Playwright and Edge/Chromium.
const {chromium} = require('playwright');
const path = require('node:path');
const fs = require('node:fs');
const assert = require('node:assert/strict');
const {pathToFileURL} = require('node:url');
async function main() {
  const root = path.resolve(__dirname, '..');
  const capture = process.argv.includes('--capture');
  const browserArg = process.argv.indexOf('--browser');
  const browser = await chromium.launch({headless:true, ...(browserArg >= 0 ? {executablePath:process.argv[browserArg+1]} : {})});
  const page = await browser.newPage({viewport:{width:1440,height:1100}});
  const errors=[]; page.on('pageerror',e=>errors.push(e.message));
  let checks=0;
  const verify=(condition,message)=>{assert(condition,message);checks++;};
  const shot=async name=>{if(capture){
    await page.evaluate(()=>document.querySelector('#toast').classList.remove('visible'));
    await page.screenshot({path:path.join(root,'reports/assets/prototype',name),fullPage:name==='06-league.png'});
  }};
  const go=async hash=>{await page.goto(pathToFileURL(path.join(root,'prototype/index.html')).href+hash);};
  const role=async name=>{await page.selectOption('#role',name);};
  const confirm=async()=>page.locator('#modal-form button[type=submit]').click();
  try {
    await go('#overview'); verify((await page.title()).startsWith('Arena'), 'Old branding');
    await shot('01-overview.png');
    await page.locator('nav a[data-page=competitions]').click();
    verify(await page.locator('.competition-card').count()===5,'Missing events');
    await page.selectOption('#format','Round-robin / league'); verify(await page.locator('.competition-card').count()===1,'Format filter failed');
    await page.selectOption('#format','all'); await page.fill('#search','no-such-event'); verify(await page.locator('.competition-card').count()===0,'Search failed');
    await go('#event/innovation'); await shot('02-registration.png');
    await page.click('[data-action=register]'); await confirm();
    await role('Organizer'); await page.click('[data-action=advance]'); await confirm();
    await role('Student'); await page.click('[data-action=submit-project]');
    await page.fill('[name=title]','AAU Sustainable Garden'); await page.fill('[name=url]','https://example.com/arena/project'); await page.fill('[name=description]','A student project for a sustainable university campus.'); await confirm();
    await role('Organizer'); await page.click('[data-action=advance]'); await confirm();
    await role('Judge'); await page.click('[data-action=review-project]');
    await page.fill('[name=innovation]','90'); await page.fill('[name=execution]','80'); await page.fill('[name=presentation]','70');
    verify(await page.textContent('#weighted-score')==='81.50','Weighted total incorrect'); await shot('03-scoring.png'); await confirm();
    await role('Organizer'); await page.click('[data-action=advance]'); await confirm();
    verify(await page.locator('.status.published').count()>0,'Project not published');
    await go('#event/robotics'); await role('Judge'); await shot('04-bracket.png');
    for(const index of [0,1,2]) {await page.locator(`[data-action=winner][data-match="${index}"]:not([disabled])`).first().click();await confirm();}
    verify((await page.locator('.champion strong').textContent())==='Team Alpha','Wrong knockout champion');
    await go('#event/quiz'); await role('Judge'); await shot('05-quiz.png');
    for(let i=0;i<3;i++){await page.click(`[data-action=quiz-score][data-team="${i}"]`);await page.fill('[name=points]',String(i===2?10:20));await confirm();}
    await role('Organizer');await page.click('[data-action=advance]');await confirm();await role('Student');
    verify((await page.locator('tbody tr td:first-child').allTextContents()).join(',')==='1,1,3','Quiz ties incorrect');
    await go('#event/league');await role('Judge');
    for(let i=0;i<3;i++){await page.click(`[data-action=league-score][data-match="${i}"]`);await page.fill('[name=score0]','2');await page.fill('[name=score1]',i===0?'2':'1');await confirm();}
    await role('Organizer');await shot('06-league.png');await page.click('[data-action=advance]');await confirm();await page.click('[data-action=advance]');await confirm();
    await role('Student');verify(await page.locator('table').count()===2,'Published league standings missing');
    await page.locator('nav a[data-page=results]').click();verify((await page.locator('h2').allTextContents()).some(x=>x.startsWith('Champion:')),'Results navigation missing league');
    await go('#competitions');await role('Organizer');await page.click('[data-action=create-preview]');
    await page.selectOption('#preview-type','quiz');verify(await page.locator('#preview-format option').count()===3,'Quiz compatibility wrong');
    await page.selectOption('#preview-format','Round-robin / league');await shot('07-creation.png');
    await page.selectOption('#preview-type','project');verify(await page.locator('#preview-format option').count()===1 && !(await page.locator('#preview-quiz').isVisible()),'Project preview exposes irrelevant controls');
    await page.click('[data-action=close]');
    for(const width of [1440,1100,820,390]){
      await page.setViewportSize({width,height:900});
      verify(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth),'Horizontal overflow at '+width);
    }
    verify(errors.length===0,errors.join('\n'));
    fs.mkdirSync(path.join(root,'build'),{recursive:true});
    const summary=`Executed ${checks} browser prototype checks: ${checks} passed, 0 failed.\nExecuted at: ${new Date().toISOString()}\n`;
    fs.writeFileSync(path.join(root,'build/prototype-test-summary.txt'),summary);console.log(summary);
  } finally {await browser.close();}
}
main().catch(error=>{console.error(error);process.exitCode=1;});
