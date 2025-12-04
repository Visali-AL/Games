**Olaf Pips-Solver**

This is a simple NYT Pips solver using the backtracking approach. I've applied some basic heuristics that I normally use when I solve it on my own. I was long fascinated by the idea of whether this could ever be done by a computer program, and I'm so happy I managed to do it all by myself! (Except for the Math Evaluator and Parser, for which I sought help from Claude, I still know what and how it did because I had to fix the intricate bugs:) ) 

It definitely works (at least for the easy and medium). I've tried it with more than 10 different puzzles from the NYT. It does go on, and on for Hard puzzles, it has to try out a lot of combinations, you know! I'm planning to do this with Dancing Links to see how I can minimise the runtime complexity. Very hopeful about it! 

<img width="1072" height="1955" alt="image" src="https://github.com/user-attachments/assets/c61da046-71b9-4fe1-94db-a7191b524680" />


<br> <br> <br>


And here's my dear Olaf's output <3

********* Sorted nodes based on partners_in_expression count ********* <br>
[C, A, B, F, G, E, J, N, P, D, H, I, K, L, M, O]<br>
********* Available Dominoes grouped by pip *********<br>
Pip: 0 found in Dominoes: [Domino[pip1=0, pip2=3], Domino[pip1=0, pip2=5], Domino[pip1=0, pip2=0], Domino[pip1=0, pip2=0]]<br>
Pip: 1 found in Dominoes: [Domino[pip1=1, pip2=4]]<br>
Pip: 2 found in Dominoes: [Domino[pip1=2, pip2=3], Domino[pip1=2, pip2=5]]<br>
Pip: 3 found in Dominoes: [Domino[pip1=3, pip2=0], Domino[pip1=3, pip2=2], Domino[pip1=3, pip2=6]]<br>
Pip: 4 found in Dominoes: [Domino[pip1=4, pip2=1]]<br>
Pip: 5 found in Dominoes: [Domino[pip1=5, pip2=0], Domino[pip1=5, pip2=2], Domino[pip1=5, pip2=6]]<br>
Pip: 6 found in Dominoes: [Domino[pip1=6, pip2=3], Domino[pip1=6, pip2=5]]<br>
********* Puzzle Solved *********{A=3, B=3, C=6, D=2, E=0, F=3, G=2, H=6, I=5, J=0, K=5, L=5, M=1, N=0, O=4, P=0}<br>
Remaining domino: {0=[], 1=[], 2=[], 3=[], 4=[], 5=[], 6=[]}<br>

<br><br><br>
And then the actual valid output! Olaf helped me solve it in a couple of seconds, which would have otherwise taken a few minutes. 
<img width="1170" height="1448" alt="image" src="https://github.com/user-attachments/assets/b1e20b7c-b8f0-449b-bbbf-cd84c8f64b22" />

<br><br><br>
I feel very honoured just to have read and understood Sir Donald E. Knuth's paper, written in 2000, on Dancing Links. I'm going to honour his legacy in my own way by building this solver using the DLX algorithm! (or DL-Pips to be precise)
For those who're interested
https://www.ocf.berkeley.edu/~jchu/publicportal/sudoku/0011047.pdf
