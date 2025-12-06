**Olaf Pips-Solver**

This is a simple NYT Pips solver using Backtracking. I've applied some basic heuristics that I normally use when I solve it on my own. I was long fascinated by the idea of whether this could ever be done by a computer program, and I'm so happy I managed to do it all by myself! (Except for the Math Evaluator and Parser, for which I sought help from Claude, I still know what and how it did because I had to fix the intricate bugs:) ) 

It definitely works (at least for the easy and medium). I've tried it with more than 10 different puzzles from the NYT. It does go on, and on for Hard puzzles, it has to try out a lot of combinations, you know! I'm planning to do this with Dancing Links to see how I can minimize the runtime complexity. Very hopeful about it! 

Update: I've now implemented the Dancing Links algorithm for Pips! It works like a charm and solves even hard puzzles in milliseconds.

<img width="1072" height="1955" alt="image" src="https://github.com/user-attachments/assets/c61da046-71b9-4fe1-94db-a7191b524680" />


<br> <br> <br>

And here's my dear Olaf's output <3

![img.png](img.png)

<br><br><br>
And then one of the accepted answers
<img width="1170" height="1448" alt="image" src="https://github.com/user-attachments/assets/b1e20b7c-b8f0-449b-bbbf-cd84c8f64b22" />

<br><br><br>
Olaf helped me solve it in a second, which would have otherwise taken a few minutes. Although definitely not as impressive as the Dancing Links version, I'm still very happy with this Backtracking version of mine! And like I said, this doesn't work for all puzzles, especially the hard ones (... it just keeps running). But I'm still very happy with it.

<br><br><br>
I feel very honoured just to have read Sir Donald E. Knuth's paper, written in 2000, on Dancing Links. I'm beyond words now having built this solver using the DLX algorithm! 
For those who're interested
https://www.ocf.berkeley.edu/~jchu/publicportal/sudoku/0011047.pdf

Here's a peek into the DLX Pips Solver's answer for the same puzzle that Olaf solved. <br>
JUST 29ms! And even to find all possible solutions, it does not go beyond 50ms.

![img_1.png](img_1.png)

I took out a specific puzzle that was referenced as one of the hardest puzzles by Paul Brown here (https://github.com/prb/pips-solver/tree/main) I just wanted to try out my DLX Pips on it, and it gave amazing results yet another time! It solved for one solution within 67 ms and all 43200 solutions in 2.7 seconds


