<h1>Group Project - Phase 1 Plan:</h1>

<h2>1) Wire Frames/ Story Board:</h2>
  <p align="center">
    <a href="https://github.com/user-attachments/assets/3a23e52f-3a01-4110-aeab-f03f8fda96bb">
      <img src="https://github.com/user-attachments/assets/3a23e52f-3a01-4110-aeab-f03f8fda96bb"
           alt="Wireframe: splash, gallery, draw screen" width="48%">
    </a>
    <a href="https://github.com/user-attachments/assets/63982920-1474-448a-8a2b-85767701cc19">
      <img src="https://github.com/user-attachments/assets/63982920-1474-448a-8a2b-85767701cc19"
           alt="Wireframe: toolbar & shapes" width="48%">
    </a>
  </p>
  <p align="center"><em>Figures 1–2. Hand-drawn wireframes.</em></p>

<h2>2) Architecture & Key Decisions (MVVM)</h2>
<li>Espresso will be used for testing the interfacing aspect of our app</li>
<li>The view and model will be split - with the view handling the current drawing state (i.e. selected tool) while the model handles the file state and saving as such.</li>
<li>We will maintain a stack of actions to support undo/redo.</li>
<li>drawings will initially be kept in memory only, persistence later. </li>
<li>Will likely utilize bitmaps for image saving/loading</li>


<h2>3) Task Breakdown (with owners & order)</h2>
<em>Note: task assignments are more loose-plans. Likely will require some collaborations on some tasks, or changes in assignment down the line.</em>
<h3>Layouts:</h3>
<ol>
  <li>fragment_canvas.xml [Josh]</li>
  <li>activity_drawing_canvas.xml [Josh]</li>
</ol>

<h3>Classes:</h3>
<ol>
  <li>placeholder model class(something to store our bitmap, pen, size, and color?) [Alex]</li>
  <li>CanvasFragment.kt [Josh]</li>
  <li>DrawingCanvasActivity.kt [Josh, Alex, Daniel]</li>
  <li>DrawingCanvasViewModel.kt [Alex, Daniel]</li>
</ol>

<h3>Unit Tests:</h3>
<h4>Espresso Tests:</h4>
<ol>
  <li>DrawPenTest [Alex]</li>
  <li>ChangeDrawSizeLargeTest [Alex]</li>
  <li>ChangeDrawSizeMediumTest [Alex]</li>
  <li>DrawPenSmallTest [Daniel]</li>
  <li>DrawPenLargeTest [Daniel]</li>
  <li>DrawBrushSmallTest [Daniel]</li>
  <li>DrawBrushLargeTest [Daniel]</li>
  <li>RestoreDrawingTest [Alex, Daniel, Josh]</li>
</ol>
  
<h4>Model Tests</h4>
<ol>
  <li>ChangePenTest [Josh]</li>
  <li>ChangeColorTest [Josh]</li>
  <li>StoreToBitmapTest [Alex]</li>
  <li>RetrieveBitmapTest [Daniel]</li>
</ol>

<h3>Stretch Goals</h3>
Task-Assignment for these will be given at a later time. Once all core Phase 1 requirements completion.
<ol>
  <li>activity_drawing_manager.xml</li>
  <li>fragment_drawing_item.xml</li>
  <li>DrawingItemFragment.kt</li>
  <li>DrawingManagerActivity.kt</li>
  <li>SaveFileTest</li>
  <li>LoadFileTest</li>
  <li>UpdateFileTest</li>
</ol>
